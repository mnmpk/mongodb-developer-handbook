const z = require('zod');
let db;

exports.init = (conn) => {
    db = conn.getClient().db();
}
exports.findUserSecure = async (req, res) => {
    const Query = z.object({
        username: z.string(),
    });

    try {
        // the parsed result is validated and type safe!
        const { username } = Query.parse(req.body);

        // Validate and sanitize input  
        if (!username || !/^[a-z0-9]+$/i.test(username)) {
            return res.status(400).send("Invalid input: Username must be alphanumeric.");
        }

        // Run a query with sanitized input  
        const users = await db.collection("users").find({ username }).toArray();

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
exports.findUserInsecure = async (req, res) => {
    const { query } = req.body;

    try {
        // Directly use raw user input in the query (dangerous!)  
        const users = await db.collection("users").find(query).toArray();

        if (users.length > 0) {
            res.status(200).json(users);
        } else {
            res.status(404).send("No users found for the specified query.");
        }
    } catch (err) {
        console.error("Error finding user:", err);
        res.status(500).send("Internal server error.");
    }
}