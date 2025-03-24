let db;

const init = (conn) => {
    db = conn.getClient().db();
}
const put = async (req, res) => {
    const coll = db.collection('cacheObject');
    const o = { value: require('node:crypto').randomBytes(1 * 1024 * 1024 / 3).toString() };
    await coll.insertOne(o)
    res.send(o);
}
module.exports = { init, put };