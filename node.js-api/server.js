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
const { mongoCache } = require('./middlewares/mongo-cache');
var mongoStore = new MongoDBStore({
  uri: mongoUri,
  collection: 'sessions'
});
mongoStore.on('error', function (error) {
  console.log(error);
});


//Redis Store
const { RedisStore } = require("connect-redis");
const { redisCache } = require('./middlewares/redis-cache');
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

app.use(/\/mongo\/.*/, mongoCache({ client: db.getClient(), expireAfterSeconds: 600 }));
app.get('/mongo/put', require('./controllers/cache').put);
app.get('/mongo/clear', require('./controllers/cache').clear);

app.use(/\/redis\/.*/, redisCache({ client: redisClient, expireAfterSeconds: 600 }));
app.get('/redis/put', require('./controllers/cache').put);
app.get('/redis/clear', require('./controllers/cache').clear);


app.get('/login', require('./controllers/session').login);
app.get('/logout', require('./controllers/session').logout);

app.get('/session', async (req, res) => {
  if (req.session.principal) {
    req.session.shareData.search.push(req.query['s']);
    req.session.shareData.views++;
    res.status(200);
    res.write(JSON.stringify(req.session.shareData));
    res.end();
    //res.send("views: " + req.session.views);
  } else {

    res.status(200);
    res.end('Please login first');
  }
});



app.get('/stable-api', require('./controllers/stable-api').stableAPI);
app.get('/rs-status', require('./controllers/stable-api').rsStatus);

app.get('/test-save-date', require('./controllers/date').saveDate);
app.get('/test-dirty-update', require('./controllers/date').dirtyUpdate);

app.get('/watch', require('./controllers/change-stream').watch);

app.listen(3000, () => {
  console.log("Server is running at port 3000");
});

