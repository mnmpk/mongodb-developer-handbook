[
    {
      $addFields: {
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
    },
    {
      $unset: "trans"
    },
    {
      $group: {
        _id: {
          type: "$type",
          bucketSize: "$bucketSize"
        },
        accts: {
          $push: {
            acct: "$acct",
            casinoCode: "$casinoCode",
            areaCode: "$areaCode",
            sumBet: "$sumBet",
            sumCasinoWin: "$sumCasinoWin",
            sumTheorWin: "$sumTheorWin",
            noOfTxn: "$noOfTxn",
            avgBet: "$avgBet",
            avgCasinoWin: "$avgCasinoWin",
            avgTheorWin: "$avgTheorWin"
          }
        }
      }
    },
    {
      $project: {
        _id: 0,
        type: "$_id.type",
        bucketSize: "$_id.bucketSize",
        accts: "$accts"
      }
    },
    // {
    //   $group: {
    //     _id: "$_id.type",
    //     bucketSize: {
    //       $push: "$doc"
    //     }
    //   }
    // }
    // {
    //   $match:
    //     /**
    //      * query: The query in MQL.
    //      */
    //     {
    //       type: "acct-areaCode",
    //       bucketSize: "15days"
    //     }
    // }
    {
      $merge: {
        into: "tRatingFinal",
        on: ["type", "bucketSize"],
        whenMatched: "replace",
        whenNotMatched: "insert"
      }
    }
  ]