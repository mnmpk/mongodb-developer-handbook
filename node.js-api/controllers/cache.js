exports.put = async (req, res) => {
    res.send({ value: require('node:crypto').randomBytes(1 * 1024 * 1024 / 3).toString() });
}
exports.clear = async (req, res) => {
    res.send({ value: "cleared" });
}