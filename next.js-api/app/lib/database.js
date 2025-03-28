const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer";
const client = new MongoClient(uri, {
    serverApi: {
        version: ServerApiVersion.v1,
        strict: true,
        deprecationErrors: true,
    }
});

async function connect(database) {
    return (await client.connect()).db(database);
}
module.exports = { connect };