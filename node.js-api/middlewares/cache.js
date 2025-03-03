const { MongoClient } = require("mongodb");
const hash = require("object-hash");
let client, db, coll;

async function init(uri, database, collection = "cache") {

    if (!client) {
        client = new MongoClient(uri);
    }
    db = database;
    coll = collection;
    //TODO: expire option and recreate TTL index
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


async function writeData(key, data) {
    try {
        const result = await client.db(db).collection(coll).replaceOne({ _id: key }, { _id: key, v: data, cAt: new Date() }, { upsert: true });
    } catch (e) {
        console.error(`Failed to cache data for key=${key}`, e);
    }
}

async function readData(key) {
    return await client.db(db).collection(coll).findOne({ _id: key });
}

function cache() {
    return async (req, res, next) => {
        const key = requestToKey(req);
        // if there is some cached data, retrieve it and return it
        const cachedValue = await readData(key);
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
                    writeData(key, data).then();
                }

                return res.send(data);
            };

            // continue to the controller function
            next();
        }
    };
}

module.exports = { init, cache };