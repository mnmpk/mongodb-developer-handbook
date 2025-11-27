//MongoClient
const { MongoClient } = require("mongodb");

//mongoose
const mongoose = require("mongoose");
const mongoUri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer";
mongoose.connect(mongoUri,
  { serverApi: { version: '1', strict: true } }
);
const db = mongoose.connection;
db.on("error", console.error.bind(console, "connection error: "));
db.once("open", async function () {
  console.log("Connected successfully");
});

//Session
const session = require('express-session');

//Mongo Store
const MongoDBStore = require('connect-mongodb-session')(session);
var mongoStore = new MongoDBStore({
  uri: mongoUri,
  collection: 'sessions'
});
mongoStore.on('error', function (error) {
  console.log(error);
});


//Redis Store
const { RedisStore } = require("connect-redis");
const { createClient } = require("redis");
const redisUri = "rediss://clustercfg.tsp-cache.byysaj.ape1.cache.amazonaws.com:6379";
let redisClient = createClient({
  url: redisUri
})
redisClient.connect().catch(console.error)
let redisStore = new RedisStore({
  client: redisClient,
  prefix: "nodeApp:",
});



const express = require("express");
const compression = require('compression');

const app = express();

app.use(compression());
app.use(express.json());

app.use(session({
  secret: 'This is a secret',
  cookie: {
    maxAge: 1000 * 60 * 60 * 24 * 7 // 1 week
  },
  //store: mongoStore,
  store: process.env.profile == 'redis' ? redisStore : mongoStore,
  // Boilerplate options, see:
  // * https://www.npmjs.com/package/express-session#resave
  // * https://www.npmjs.com/package/express-session#saveuninitialized
  resave: true,
  saveUninitialized: true
}));

const c = require('./controllers/cache');
const { mongoCache, clearMongo } = require('./middlewares/mongo-cache');
const { redisCache, clearRedis } = require('./middlewares/redis-cache');

c.init(db);
app.use(/\/mongo\/.*/, mongoCache({ client: db.getClient(), expireAfterSeconds: 600 }));
app.get('/mongo/data', c.put);

app.use(/\/redis\/.*/, redisCache({ client: redisClient }));
app.get('/redis/data', c.put);

app.get('/clear/mongo', clearMongo({ client: db.getClient() }), c.put);
app.get('/clear/redis', clearRedis({ client: redisClient }), c.put);

const s = require('./controllers/session');
s.init(db);
app.get('/login', s.login);
app.get('/put', s.put);
app.get('/clear', s.clear);
app.get('/logout', s.logout);



app.get('/stable-api', require('./controllers/stable-api').stableAPI);
app.get('/rs-status', require('./controllers/stable-api').rsStatus);

app.get('/test-save-date', require('./controllers/date').saveDate);
app.get('/test-dirty-update', require('./controllers/date').dirtyUpdate);

app.get('/watch', require('./controllers/change-stream').watch);

const injection = require('./controllers/injection');
injection.init(db);


const { body } = require('express-validator');

// example body requests:
//{"username":"hello"}
//{"username":{"$ne":"null"}}
//{"username":"{\"$ne\":\"null\"}"}
app.use(/\/injection\/secure/, body('username').isAlphanumeric().withMessage('Username must contain only alphanumeric characters'));
app.post('/injection/secure', injection.findUser);
app.post('/injection/insecure', injection.findUser);

app.listen(3000, () => {
  console.log("Server is running at port 3000");
});

