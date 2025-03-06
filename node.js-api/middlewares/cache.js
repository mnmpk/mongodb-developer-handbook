const hash = require("object-hash");

const DEFAULT_TTL = 30;
const DEFAULT_COLL = "cache";
const INDEX_NAME = "expires_1";
let c;

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


async function writeData(database, collection, key, data, ttl) {
    try {
        const result = await c.db(database).collection(collection).replaceOne({ _id: key }, { _id: key, v: data, expires: new Date(new Date().getTime() + ttl * 1000) }, { upsert: true });
    } catch (e) {
        console.error(`Failed to cache data for key=${key}`, e);
    }
}

async function readData(database, collection, key, ttl) {
    return await c.db(database).collection(collection).findOneAndUpdate({ _id: key }, { $set: { expires: new Date(new Date().getTime() + ttl * 1000) } });
}

function init({ database = "", collection = DEFAULT_COLL, ttl = DEFAULT_TTL } = { database: "", collection: DEFAULT_COLL, ttl: DEFAULT_TTL }) {
    return async (req, res, next) => {
        const key = requestToKey(req);
        // if there is some cached data, retrieve it and return it
        const cachedValue = await readData(database, collection, key, ttl);
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
                    writeData(database, collection, key, data, ttl).then();
                }

                return res.send(data);
            };

            // continue to the controller function
            next();
        }
    };
}

function cache({ client, database = "", collection = DEFAULT_COLL, expireAfterSeconds = DEFAULT_TTL }) {
    c = client;
    c.db(database).collection(collection).createIndex({ expires: -1 }, { name: INDEX_NAME, expireAfterSeconds: 0 });
    return init({ database, collection, ttl: expireAfterSeconds });
}

module.exports = { cache };