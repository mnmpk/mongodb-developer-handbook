let db;

const init = (conn) => {
    db = conn.getClient().db();
}
const login = async (req, res) => {
    req.session.principal = req.query['uId'];

    //Populate other channel's data
    let temp = { channel: "web", views: 1, search: [] };
    const coll = db.collection('sessions');
    const shareData = await coll.findOne({ "principal": req.session.principal, "attrs.shareData.channel": "mob" });
    if (shareData && shareData.attrs && shareData.attrs.shareData) {
        temp.search = temp.search.concat(shareData.attrs.shareData.search[1]);
        temp.views += shareData.attrs.shareData.views;
    }
    req.session.shareData = temp;

    res.send(req.session);
}
const put = async (req, res) => {
    if(req.session.shareData){
        req.session.shareData.search.push(req.query['s']);
        req.session.shareData.views++;
    }
    res.send(req.session);
}
const clear = async (req, res) => {
    req.session.shareData = { channel: "web", views: 1, search: [] };
    res.send(req.session);
}
const logout = async (req, res) => {
    req.session.destroy();
    res.send(req.session);
}
module.exports = { init, login, put, clear, logout };