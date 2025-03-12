const { MongoClient } = require("mongodb");
const express = require("express");
const mongoose = require("mongoose");
const session = require('express-session');
const MongoDBStore = require('connect-mongodb-session')(session);

const User = require('./user');
const { cache } = require('./middlewares/cache');

const app = express();

app.use(express.json());

//const uri = "mongodb://localhost:27017/test";
const uri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer"
mongoose.connect(uri,
  { serverApi: { version: '1', strict: true } }
);
const db = mongoose.connection;
db.on("error", console.error.bind(console, "connection error: "));
db.once("open", async function () {
  console.log("Connected successfully");
});


var store = new MongoDBStore({
  uri: uri,
  collection: 'sessions'
});
store.on('error', function (error) {
  console.log(error);
});

app.use(session({
  secret: 'This is a secret',
  cookie: {
    maxAge: 1000 * 60 * 60 * 24 * 7 // 1 week
  },
  store: store,
  // Boilerplate options, see:
  // * https://www.npmjs.com/package/express-session#resave
  // * https://www.npmjs.com/package/express-session#saveuninitialized
  resave: true,
  saveUninitialized: true
}));
app.use(cache({ client: db.getClient(), expireAfterSeconds: 600 }));

app.get('/test-cache', async (req, res) => {
  res.send("test cache: " + Math.random());
});
app.get('/test-cache2', cache({ client: db.getClient(), collection: 'cache2' }), async (req, res) => {
  res.send("test cache: " + Math.random());
});



app.get('/login', async (req, res) => {
  req.session.principal = req.query['uId'];

  //Populate other channel's data
  let temp =  { channel: "web", views: 1, search: [] };
  const coll = db.getClient().db().collection('sessions');
  const shareData = await coll.findOne({ "principal": req.session.principal, "attrs.shareData.channel": "mob" });
  temp.search=temp.search.concat(shareData.attrs.shareData.search[1]);
  temp.views += shareData.attrs.shareData.views;
  req.session.shareData = temp;


  res.status(200);
  res.end('welcome to the session demo.');
});

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


const saveDate = async (name, date) => {
  let user = new User({
    name: name,
    lastActiveAt: date
  })
  console.log(user);
  user = await user.save();
  user = await User.findById(user._id).exec();
  console.log("input: " + date);
  console.log("output: " + user.lastActiveAt);
  console.log("output json: " + user);
  console.log("createdAt: " + user.createdAt);
  console.log("toISOString: " + user.lastActiveAt.toISOString());
  console.log("toUTCString: " + user.lastActiveAt.toUTCString());
  console.log("toLocaleString: " + user.lastActiveAt.toLocaleString());
  console.log("toDateString: " + user.lastActiveAt.toDateString());
}

const updateWithVer = async (id, version, name) => {

  console.log("Update name to: " + name);
  if (await User.findOneAndUpdate({ _id: id, version: version }, { $set: { name: name }, $inc: { version: 1 } })) {
    console.log("success");
  } else {
    console.log("failed. document no found.");
  }
}

app.get('/stable-api', async (req, res) => {
  const clientOptions = [
    { serverApi: { version: '1' } },
    //APIStrictError
    { serverApi: { version: '1', strict: true } },
    //APIDeprecationError
    { serverApi: { version: '1', strict: true, deprecationErrors: true } },
    //APIVersionError
    { serverApi: { version: '99' } },
    //InvalidOptions
    { serverApi: { strict: true, deprecationErrors: true } }
  ]
  clientOptions.forEach(async option => {
    try {
      const c = new MongoClient(uri,
        option
      );
      console.log(option, await c.db('admin').command({ "replSetGetStatus": 1 }));
    } catch (err) {
      console.log(option, err);
    }
  });
  res.send();
});
app.get('/rs-status', async (req, res) => {
  const adminDB = db.getClient().db('admin');
  res.send(await adminDB.command({ "replSetGetStatus": 1 }));
});

app.get('/test-save-date', async (req, res) => {
  await saveDate("String date", '2023-05-17');
  await saveDate("JS date", new Date(2023, 4, 17));
  await saveDate("Number date", 1232345);
  try {
    await saveDate("Not a date", "123dfds456");
  } catch (ex) {
    console.error(ex);
  }
  res.send(await User.find({}));
});

app.get('/test-dirty-update', async (req, res) => {
  let u = new User({
    name: "M Ma",
    lastActiveAt: new Date(),
    version: 1
  })
  u = await u.save();
  await updateWithVer(u.id, u.version, "M");
  await updateWithVer(u.id, u.version, "Ma");

  u = await User.findById(u._id).exec();
  await updateWithVer(u.id, u.version, "MMa");
  res.send((await User.findById(u.id).exec()).toJsonWithLocalDate());
});

app.get('/watch', async (req, res) => {
  const client = mongoose.connection.getClient();
  const snCollection = client.db('serviceNotificationDB').collection('serviceNotifications');
  snCollection.watch([]).on('change', async (data) => {
    try {
      const _id = data.documentKey._id.toString();
      const operationType = data.operationType;
      const updateDescription = data.updateDescription ? data.updateDescription : {};

      throw new Exception("error");
      switch (operationType) {
        case 'update':
          let updatedFields = updateDescription.updatedFields ? Object.keys(updateDescription.updatedFields) : [];
          let removedFields = updateDescription.removedFields;
          if (removedFields?.length) { updatedFields = updatedFields.concat(removedFields); }

          for (let field of updatedFields) {
            console.log(`Updating SN filterObject, id: ${_id}`);
            break;
          }

          for (let field of ['status', 'specialRequestRuleList']) {
            if (updatedFields.includes(field)) {
              console.log(`Updating SSA reminder date, id: ${_id}`);
              break;
            }
          }
          break;
        case 'insert':
          console.log(`Updating SN filterObject, id: ${_id}`);
          console.log(`Updating SN SSA reminder date, id: ${_id}`);
          break;
        default:
          console.log(data);
          break;
      }
    } catch (error) {
      console.error(error);
    }


  });
  res.send("watching serviceNotifications");
});

app.listen(3000, () => {
  console.log("Server is running at port 3000");
});

