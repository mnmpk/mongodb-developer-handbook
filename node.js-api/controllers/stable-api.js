exports.stableAPI=async (req, res) => {
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
  }

  exports.rsStatus=async (req, res) => {
    const adminDB = db.getClient().db('admin');
    res.send(await adminDB.command({ "replSetGetStatus": 1 }));
  }