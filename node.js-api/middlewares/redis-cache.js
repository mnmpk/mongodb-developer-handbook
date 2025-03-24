const hash = require("object-hash");

let c;

function requestToKey(req) {
    // build a custom object to use as part of the Redis key
    const reqDataToHash = {
        query: req.query,
        body: req.body,
    };

    // `${req.path}@...` to make it easier to find
    // keys on a Redis client
    //return `${req.path}@${hash.sha1(reqDataToHash)}`;
    return `${hash.sha1(reqDataToHash)}`;
}


async function writeData(key, data) {
    try {
        await c.set(key, JSON.stringify(data));
    } catch (e) {
        console.error(`Failed to cache data for key=${key}`, e);
    }
}

async function readData(key, ttl) {
    return JSON.parse(await c.get('key'));
}

function init() {
    return async (req, res, next) => {
        const key = requestToKey(req);
        // if there is some cached data, retrieve it and return it
        const cachedValue = await readData(key);
        console.log(cachedValue);
        if (cachedValue && cachedValue.v) {
            return res.send(cachedValue.v);
        } else {
            // override how res.send behaves
            // to introduce the caching logic
            const oldSend = res.send;
            res.send = function (data) {
                // set the function back to avoid the 'double-send' effect
                res.send = oldSend;
                // cache the response only if it is successful
                if (res.statusCode.toString().startsWith("2")) {
                    writeData(key, {v:data}).then();
                }

                return res.send(data);
            };

            // continue to the controller function
            next();
        }
    };
}

function redisCache({ client }) {
    c = client;
    return init();
}
function clearRedis({ client }) {
    return async (req, res, next) => {
        const key = requestToKey(req);
        client.del(key);
        next();
    };
}

module.exports = { redisCache, clearRedis };