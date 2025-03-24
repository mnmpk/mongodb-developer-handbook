let db;

const init = (conn) => {
    db = conn.getClient().db();
}
const put = async (req, res) => {
    const coll = db.collection('cacheObject');
    const o = { value: generateRandomString(req.query['size']).toString() };
    await coll.insertOne(o)
    res.send(o);
}
module.exports = { init, put };

function generateRandomString(length) {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    for ( let i = 0; i < length; i++ ) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}