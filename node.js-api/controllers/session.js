exports.login = async (req, res) => {
    req.session.principal = req.query['uId'];

    //Populate other channel's data
    let temp = { channel: "web", views: 1, search: [] };
    const coll = db.getClient().db().collection('sessions');
    const shareData = await coll.findOne({ "principal": req.session.principal, "attrs.shareData.channel": "mob" });
    if (shareData && shareData.attrs && shareData.attrs.shareData) {
        temp.search = temp.search.concat(shareData.attrs.shareData.search[1]);
        temp.views += shareData.attrs.shareData.views;
    }
    req.session.shareData = temp;


    res.status(200);
    res.end('welcome to the session demo.');
}
exports.logout = async (req, res) => {
    req.session.destroy();
    res.status(200);
    res.end('logged out');
}