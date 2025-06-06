package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class TableRating {
    private Long tranID;
    private Integer playerId;
    private Integer isVoid;
    private Integer tranCodeID;
    private Date gamingDt;
    private Integer shift;
    private Date agingDt;
    private Date postDtm;
    private String computerName;
    private Integer empID;
    private Integer authEmpId;
    private Integer deptID;
    private Integer casinoID;
    private Integer areaID;
    private Integer locnID;
    private Integer gameID;
    private Integer denomID;
    private Integer repID;
    private String documentNo;
    private String ref1;
    private String ref2;
    private Integer isOpenItem;
    private Integer groupID;
    private Integer playerTypeID;
    private Integer originalPlayerID;
    private Long relatedTranID;
    private Long voidTranID;
    private String flags;
    private Boolean isDistributed;
    private Long oldRelatedTranID;
    private Integer stratId;
    private Date ratingStartDtm;
    private Date ratingEndDtm;
    private Integer cashBuyIn;
    private Integer creditBuyIn;
    private Integer chipBuyIn;
    private Integer promoBuyIn;
    private Integer eCreditBuyIn;
    private Integer eCashBuyIn;
    private Integer ratingPeriodMinutes;
    private Double plays;
    private Double bet;
    private Integer paidOut;
    private Integer walkedWith;
    private Integer theorHoldPc;
    private Double theorWin;
    private Integer gamePts;
    private String casinoWinCalcMethod;
    private Double casinoWin;
    private Integer highBet;
    private Integer locnMinBet;
    private Integer locnMaxBet;
    private Integer casinoStatistic;
    private Date createdDtm;
    private Integer createdBy;
    private Date modifiedDtm;
    private Integer modifiedBy;
    private Integer seatNo;
    private Integer freqId;
    private Double adjBet;
    private Integer adjPaidOut;
    private Double adjTheorWin;
    private Double adjCasinoWin;
    private Integer dataRowVersion;
    private Date actualRatingStartTime;
    private Date actualRatingEndTime;
    private Date tripDt;
    private Integer tripId;
    private String tripType;
    private Date allowedPurgeDt;
    private Integer vIPPlayerID;
    private Integer betByBet;
    private Integer isBetByBet;
    private Integer averageBet;
    private String ratingCategory;
    private String chipSetType;
}
