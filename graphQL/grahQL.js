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
    sumBet: Float
    sumCasinoWin: Float
    sumTheorWin: Float
    noOfTxn: Int
    avgBet: Float
    avgCasinoWin: Float
    avgTheorWin: Float
  }
  # The "Query" type is special: it lists all of the available queries that
  # clients can execute, along with the return type for each. In this
  # case, the "books" query returns an array of zero or more Books (defined above).
  type Query {
    getAccountArea15days: [stat]
    getAccountArea3mins: [stat]
    getAccountCasinoArea1day: [stat]
    getAccountCasinoArea3mins: [stat]
    getAccountCasino1day: [stat]
  }

  type Subscription {
    watchAccountArea15days: stat
    watchAccountArea3mins: stat
    watchAccountCasinoArea1day: stat
    watchAccountCasinoArea3mins: stat
    watchAccountCasino1day: stat
  }
`;