[
  /*  {
      $match: {
        playerId: 894518610
      }
    },*/
  {
    $lookup: {
      from: "tLocn",
      localField: "locnID",
      foreignField: "locnId",
      as: "locns"
    }
  },
  {
    $lookup: {
      from: "tCasino",
      localField: "casinoID",
      foreignField: "casinoId",
      as: "casinos"
    }
  },
  {
    $lookup: {
      from: "tDept",
      localField: "deptID",
      foreignField: "deptId",
      as: "depts"
    }
  },
  {
    $lookup: {
      from: "tArea",
      localField: "areaID",
      foreignField: "areaId",
      as: "areas"
    }
  },
  {
    $lookup: {
      from: "tGame",
      localField: "gameID",
      foreignField: "gameId",
      as: "games"
    }
  },
  {
    $lookup: {
      from: "tPlayerCard",
      localField: "playerId",
      foreignField: "playerId",
      as: "playerCards"
    }
  },
  {
    $lookup: {
      from: "tAwards",
      localField: "tranID",
      foreignField: "relatedTranId",
      pipeline: [
        {
          $lookup: {
            from: "tPlayerPoints",
            localField: "tranId",
            foreignField: "tranId",
            /*pipeline: [
          {
            $match: {
              bucketGroupId: 2
            }
          }
        ],*/
            as: "playerPoints"
          }
        },
        {
          $lookup: {
            from: "tPlayerComps",
            localField: "tranId",
            foreignField: "tranId",
            as: "playerComps"
          }
        }
      ],
      as: "awards"
    }
  },
  {
    $match: {
      awards: {
        $ne: []
      },
      "awards.playerPoints": {
        $ne: []
      },
      "awards.playerComps": {
        $ne: []
      }
    }
  },
  {
    $project: {

      /*tr.TranID, tr.GamingDt,tr.PostDtm,pc.Acct, l.LocnCode, a.AreaCode, c.CasinoCode, g.GameCode,l.LocnInfo3,l.LocnInfo4, d.DeptCode, tr.RatingCategory, tr.TheorWin , tr.CasinoWin, tr.Bet*/


      tranID: "$tranID",
      gamingDt: "$gamingDt",
      bucketDt3mins: {
        $dateTrunc: {
          date: "$gamingDt",
          binSize: 3,
          unit: "minute"
        }
      },
      bucketDt1day: {
        $dateTrunc: {
          date: "$gamingDt",
          binSize: 1,
          unit: "day"
        }
      },
      bucketDt15days: {
        $dateTrunc: {
          date: "$gamingDt",
          binSize: 15,
          unit: "day"
        }
      },
      postDtm: "$postDtm",
      ratingCategory: "$ratingCategory",
      theorWin: "$theorWin",
      casinoWin: "$casinoWin",
      bet: "$bet",
      acct: {
        $first: "$playerCards.acct"
      },
      locnCode: {
        $first: "$locns.locnCode"
      },
      areaCode: {
        $first: "$areas.areaCode"
      },
      casinoCode: {
        $first: "$casinos.casinoCode"
      },
      gameCode: {
        $first: "$games.gameCode"
      },
      locnInfo3: {
        $first: "$locns.locnInfo3"
      },
      locnInfo4: {
        $first: "$locns.locnInfo4"
      },
      deptCode: {
        $first: "$depts.deptCode"
      },
    }
  }
]