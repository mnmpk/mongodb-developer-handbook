const { MongoClient } = require("mongodb");
const hash = require("object-hash");

const DEFAULT_TTL = 30;
const DEFAULT_COLL = "cache";
const INDEX_NAME = "_ttl";
let client;

async function init({ uri = "", database = "", collection = DEFAULT_COLL, ttl = DEFAULT_TTL }) {
    if (!client) {
        client = new MongoClient(uri);
    }
    const coll = await client.db(database).createCollection(collection);
    (await coll.listIndexes().toArray()).forEach(async i => {

        if (i.name == INDEX_NAME && i.expireAfterSeconds != ttl) {
            await client.db(database).collection(collection).dropIndex(INDEX_NAME);
        }
    });
    await client.db(database).collection(collection).createIndex({ cAt: -1 }, { name: INDEX_NAME, expireAfterSeconds: ttl });
}
function requestToKey(req) {
    // build a custom object to use as part of the Redis key
    const reqDataToHash = {
        query: req.query,
        body: req.body,
    };

    // `${req.path}@...` to make it easier to find
    // keys on a Redis client
    return `${req.path}@${hash.sha1(reqDataToHash)}`;
}


async function writeData(database, collection, key, data) {
    try {
        const result = await client.db(database).collection(collection).replaceOne({ _id: key }, { _id: key, v: data, cAt: new Date() }, { upsert: true });
    } catch (e) {
        console.error(`Failed to cache data for key=${key}`, e);
    }
}

async function readData(database, collection, key) {
    return await client.db(database).collection(collection).findOne({ _id: key });
}

function cache({ database = "", collection = DEFAULT_COLL } = { database: "", collection: DEFAULT_COLL }) {
    return async (req, res, next) => {
        const key = requestToKey(req);
        // if there is some cached data, retrieve it and return it
        const cachedValue = await readData(database, collection, key);
        if (cachedValue && cachedValue.v) {
            try {
                // if it is JSON data, then return it
                return res.json(JSON.parse(cachedValue.v));
            } catch {
                // if it is not JSON data, then return it
                return res.send(cachedValue.v);
            }
        } else {
            // override how res.send behaves
            // to introduce the caching logic
            const oldSend = res.send;
            res.send = function (data) {
                // set the function back to avoid the 'double-send' effect
                res.send = oldSend;
                // cache the response only if it is successful
                if (res.statusCode.toString().startsWith("2")) {
                    writeData(database, collection, key, data).then();
                }

                return res.send(data);
            };

            // continue to the controller function
            next();
        }
    };
}

module.exports = { init, cache };