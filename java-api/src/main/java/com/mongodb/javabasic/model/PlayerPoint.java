package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class PlayerPoint {
    private Integer playerId;
    private Long tranId;
    private Integer tranCodeId;
    private Integer gamePts;
    private Integer basePts;
    private Integer bonusPts;
    private Integer adjPtsCr;
    private Integer adjPtsDr;
    private Integer redeemPts;
    private Integer expirePts;
    private Double partialPts;
    private Double partialPts2;
    private Integer ptsBal;
    private Integer overPts;
    private Date createdDtm;
    private Integer createdBy;
    private Date modifiedDtm;
    private Integer modifiedBy;
    private Integer qualPts;
    private Integer bucketGroupId;
    private Date gamingDt;
    private Integer dataRowVersion;
    private Date expiryDate;
    private Integer partialPt1Overflow;
    private Integer partialPt2Overflow;
}
