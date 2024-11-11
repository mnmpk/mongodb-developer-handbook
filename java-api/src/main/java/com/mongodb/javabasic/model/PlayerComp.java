package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class PlayerComp {
    private Integer playerId;
    private Long tranId;
    private Integer tranCodeId;
    private Double earnedComp;
    private Integer earnedAltComp;
    private Integer adjCompCr;
    private Integer adjCompDr;
    private Integer compUsed;
    private Double compBal;
    private Integer altCompBal;
    private Integer expireComp;
    private Integer expireAltComp;
    private Integer overAltComp;
    private Integer overComp;
    private Date createdDtm;
    private Integer createdBy;
    private Date modifiedDtm;
    private Integer modifiedBy;
    private Integer bucketGroupId;
    private Date gamingDt;
    private Integer dataRowVersion;
    private Integer bonusComps;
    private Date expiryDate;
}
