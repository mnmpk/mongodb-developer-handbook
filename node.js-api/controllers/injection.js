const { validationResult, matchedData } = require('express-validator');
let db;

exports.init = (conn) => {
    db = conn.getClient().db();
}
exports.findUser = async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }
    try {
        const data = matchedData(req);
        // Run a query with sanitized input  
        const users = await db.collection("users").find(data).toArray();

        if (users.length > 0) {
            res.status(200).json(users);
        } else {
            res.status(404).send("No users found with the specified username.");
        }
    } catch (err) {
        console.error("Error finding user:", err);
        res.status(500).send("Internal server error.");
    }
}
