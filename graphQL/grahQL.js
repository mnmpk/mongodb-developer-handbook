export const typeDefs = `#graphql
  # Comments in GraphQL strings (such as this one) start with the hash (#) symbol.
  scalar Decimal

  type stat {
    _id : String
    type: String
    bucketSize: String
    acct: String
    casinoCode: String
    areaCode: String
    locnCode: String
    sumBet: Float
    sumCasinoWin: Float
    sumTheorWin: Float
    noOfTxn: Int
    avgBet: Float
    avgCasinoWin: Float
    avgTheorWin: Float
  }

  type summary {
    locnIndex: Int
    locnCode: String
    headCount: Int
    areaCode: String
    casinoCode: String
  }

  # The "Query" type is special: it lists all of the available queries that
  # clients can execute, along with the return type for each. In this
  # case, the "books" query returns an array of zero or more Books (defined above).
  type Query {
    getAccountArea15days: [stat]
    getAccountArea1day: [stat]
    getAccountArea3mins: [stat]
    getAccountCasinoArea15days: [stat]
    getAccountCasinoArea1day: [stat]
    getAccountCasinoArea3mins: [stat]
    getAccountCasino15days: [stat]
    getAccountCasino1day: [stat]
    getAccountCasino3mins: [stat]
    getCasinoAreaLocation1day: [summary]
  }

  type Subscription {
    watchAccountArea15days: stat
    watchAccountArea1day: stat
    watchAccountArea3mins: stat
    watchAccountCasinoArea15days: stat
    watchAccountCasinoArea1day: stat
    watchAccountCasinoArea3mins: stat
    watchAccountCasino15days: stat
    watchAccountCasino1day: stat
    watchAccountCasino3mins: stat
    watchCasinoAreaLocation1day: summary
  }
`;