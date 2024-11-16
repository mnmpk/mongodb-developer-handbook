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
  }]).toArray();
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
  }]).toArray();
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
  }]).toArray();
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
  }]).toArray();
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
  }]).toArray();
  return _result;
}
export const getCasinoAreaLocation1day = async () => {
  const _result = await mongoDBOperation.tRatingBucket.aggregate([{ '$match': { 'type': 'casinoCode-areaCode-locnCode', 'bucketSize': '1day' } }, {
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
  }]).toArray();
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}
export const watchCasinoAreaLocation1day = (theHandler) => {
  mongoDBOperation.tRatingBucket.watch([{ '$match': { 'fullDocument.type': 'casinoCode-areaCode-locnCode', 'fullDocument.bucketSize': '1day' } }, {
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
  }], { fullDocument: 'updateLookup' }).on("change", next => {
    theHandler(next.fullDocument);
  });
}