import { MongoClient } from "mongodb";

const mongoDBOperation = {
  mongoDBConn: null,
  db: null,
  tRatingBucket: null,
  connectionString: process.env.MONGODB_CONNECTION_STRING,
  dbName: process.env.MONGODB_DBNAME,
}

export const getDBConnection = () => {
  // set up our database details, instantiate our connection,
  // and return that database connection
  if (!mongoDBOperation.mongoDBConn) {
    mongoDBOperation.mongoDBConn = new MongoClient(mongoDBOperation.connectionString);
    mongoDBOperation.db = mongoDBOperation.mongoDBConn.db(mongoDBOperation.db);
    mongoDBOperation.tRatingBucket = mongoDBOperation.db.collection(process.env.MONGODB_COLNAME);
    mongoDBOperation.tRatingFinal = mongoDBOperation.db.collection(process.env.MONGODB_FINAL_COLNAME);
  }
  return mongoDBOperation.mongoDBConn;
}

export const getAccountArea15days = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'acct-areaCode', 'bucketSize': '15days' } }, {
    '$addFields': {
      noOfTxn: {
        $size: "$trans"
      },
      avgBet: {
        $avg: "$trans.bet"
      },
      avgCasinoWin: {
        $avg: "$trans.casinoWin"
      },
      avgTheorWin: {
        $avg: "$trans.theorWin"
      }
    }
  },{$unset:"trans"}]).toArray();
  return _result;
}
export const getAccountArea3mins = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'acct-areaCode', 'bucketSize': '3mins' } }, {
    '$addFields': {
      noOfTxn: {
        $size: "$trans"
      },
      avgBet: {
        $avg: "$trans.bet"
      },
      avgCasinoWin: {
        $avg: "$trans.casinoWin"
      },
      avgTheorWin: {
        $avg: "$trans.theorWin"
      }
    }
  },{$unset:"trans"}]).toArray();
  return _result;
}

export const getAccountCasinoArea1day = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'acct-casinoCode-areaCode', 'bucketSize': '1day' } }, {
    '$addFields': {
      noOfTxn: {
        $size: "$trans"
      },
      avgBet: {
        $avg: "$trans.bet"
      },
      avgCasinoWin: {
        $avg: "$trans.casinoWin"
      },
      avgTheorWin: {
        $avg: "$trans.theorWin"
      }
    }
  },{$unset:"trans"}]).toArray();
  return _result;
}
export const getAccountCasinoArea3mins = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'acct-casinoCode-areaCode', 'bucketSize': '3mins' } }, {
    '$addFields': {
      noOfTxn: {
        $size: "$trans"
      },
      avgBet: {
        $avg: "$trans.bet"
      },
      avgCasinoWin: {
        $avg: "$trans.casinoWin"
      },
      avgTheorWin: {
        $avg: "$trans.theorWin"
      }
    }
  },{$unset:"trans"}]).toArray();
  return _result;
}

export const getAccountCasino1day = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'acct-casinoCode', 'bucketSize': '1day' } }, {
    '$addFields': {
      noOfTxn: {
        $size: "$trans"
      },
      avgBet: {
        $avg: "$trans.bet"
      },
      avgCasinoWin: {
        $avg: "$trans.casinoWin"
      },
      avgTheorWin: {
        $avg: "$trans.theorWin"
      }
    }
  },{$unset:"trans"}]).toArray();
  return _result;
}
export const getCasinoAreaLocation1day = async () => {
  const _result = await mongoDBOperation.tRatingFinal.aggregate([{'$addFields': {
    locnIndex: {
      $abs: {
        $mod: [
          {
            $toHashedIndexKey: "$locnCode"
          },
          100
        ]
      }
    }
  }}]).toArray();
  return _result;
}

export const watchAccountArea15days = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'acct-areaCode', 'fullDocument.bucketSize': '15days' } }, {
    '$addFields': {
      'fullDocument.noOfTxn': {
        $size: "$fullDocument.trans"
      },
      'fullDocument.avgBet': {
        $avg: "$fullDocument.trans.bet"
      },
      'fullDocument.avgCasinoWin': {
        $avg: "$fullDocument.trans.casinoWin"
      },
      'fullDocument.avgTheorWin': {
        $avg: "$fullDocument.trans.theorWin"
      }
    }
  },{$unset:"fullDocument.trans"}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}

export const watchAccountArea3mins = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'acct-areaCode', 'fullDocument.bucketSize': '3mins' } }, {
    '$addFields': {
      'fullDocument.noOfTxn': {
        $size: "$fullDocument.trans"
      },
      'fullDocument.avgBet': {
        $avg: "$fullDocument.trans.bet"
      },
      'fullDocument.avgCasinoWin': {
        $avg: "$fullDocument.trans.casinoWin"
      },
      'fullDocument.avgTheorWin': {
        $avg: "$fullDocument.trans.theorWin"
      }
    }
  },{$unset:"fullDocument.trans"}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}

export const watchAccountCasinoArea1day = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'acct-casinoCode-areaCode', 'fullDocument.bucketSize': '1day' } }, {
    '$addFields': {
      'fullDocument.noOfTxn': {
        $size: "$fullDocument.trans"
      },
      'fullDocument.avgBet': {
        $avg: "$fullDocument.trans.bet"
      },
      'fullDocument.avgCasinoWin': {
        $avg: "$fullDocument.trans.casinoWin"
      },
      'fullDocument.avgTheorWin': {
        $avg: "$fullDocument.trans.theorWin"
      }
    }
  },{$unset:"fullDocument.trans"}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}
export const watchAccountCasinoArea3mins = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'acct-casinoCode-areaCode', 'fullDocument.bucketSize': '3mins' } }, {
    '$addFields': {
      'fullDocument.noOfTxn': {
        $size: "$fullDocument.trans"
      },
      'fullDocument.avgBet': {
        $avg: "$fullDocument.trans.bet"
      },
      'fullDocument.avgCasinoWin': {
        $avg: "$fullDocument.trans.casinoWin"
      },
      'fullDocument.avgTheorWin': {
        $avg: "$fullDocument.trans.theorWin"
      }
    }
  },{$unset:"fullDocument.trans"}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}

export const watchAccountCasino1day = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'acct-casinoCode', 'fullDocument.bucketSize': '3mins' } }, {
    '$addFields': {
      'fullDocument.noOfTxn': {
        $size: "$fullDocument.trans"
      },
      'fullDocument.avgBet': {
        $avg: "$fullDocument.trans.bet"
      },
      'fullDocument.avgCasinoWin': {
        $avg: "$fullDocument.trans.casinoWin"
      },
      'fullDocument.avgTheorWin': {
        $avg: "$fullDocument.trans.theorWin"
      }
    }
  },{$unset:"fullDocument.trans"}], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}
export const watchCasinoAreaLocation1day = (theHandler) => {
  mongoDBOperation.tRatingFinal.watch([{
    '$addFields': {
      'fullDocument.locnIndex': {
        $abs: {
          $mod: [
            {
              $toHashedIndexKey: "$fullDocument.locnCode"
            },
            100
          ]
        }
      }
    }
  }], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}