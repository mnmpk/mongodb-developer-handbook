import { MongoClient } from "mongodb";

const mongoDBOperation = {
  mongoDBConn: null,
  db: null,
  tRatingBucket: null,
  connectionString: process.env.MONGODB_CONNECTION_STRING,
  dbName: process.env.MONGODB_DBNAME,
  tRatingBucketName: process.env.MONGODB_COLNAME,
}

export const getDBConnection = () => {
  // set up our database details, instantiate our connection,
  // and return that database connection
  if (!mongoDBOperation.mongoDBConn) {
    mongoDBOperation.mongoDBConn = new MongoClient(mongoDBOperation.connectionString);
    mongoDBOperation.db = mongoDBOperation.mongoDBConn.db(mongoDBOperation.db);
    mongoDBOperation.tRatingBucket = mongoDBOperation.db.collection(mongoDBOperation.tRatingBucketName);
  }
  return mongoDBOperation.mongoDBConn;
}

export const getAccountArea = async () => {
  const _result = await mongoDBOperation.tRatingBucket.find({type:'acct-areaCode'}).toArray();
  return _result;
}

export const getAccountCasinoArea = async () => {
  const _result = await mongoDBOperation.tRatingBucket.find({type:'acct-casinoCode-areaCode'}).toArray();
  return _result;
}

export const getAccountCasino = async () => {
  const _result = await mongoDBOperation.tRatingBucket.find({type:'acct-casinoCode'}).toArray();
  return _result;
}

export const watchAccountArea = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{'$match':{'fullDocument.type':'acct-areaCode'}}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}

export const watchAccountCasinoArea = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{'$match':{'fullDocument.type':'acct-casinoCode-areaCode'}}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}

export const watchAccountCasino = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{'$match':{'fullDocument.type':'acct-casinoCode'}}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}