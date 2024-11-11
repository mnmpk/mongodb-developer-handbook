package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class Award {

    private Integer playerId;
    private Long tranId;
    private Integer prizeId;
    private Integer prizeQty;
    private Integer tranCodeId;
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
    private Integer groupID;
    private Integer playerTypeID;
    private String documentNo;
    private String ref1;
    private String ref2;
    private Integer authAward;
    private Integer awardUsed;
    private String awardCode;
    private Long relatedTranId;
    private Long voidTranId;
    private Integer voidEmpId;
    private Integer voidAuthEmpId;
    private Integer isVoid;
    private Integer isOpenItem;
    private Integer flags;
    private String outlet;
    private String itemCode;
    private Integer isDistributed;
    private Integer sequenceId;
    private Long oldRelatedTranID;
    private Integer siteId;
    private String serverWorkStation;
    private Integer dataRowVersion;
    private Integer playerSessionId;
    private Integer promotionId;
    private String tripDt;
    private Integer tripId;
    private String tripType;
    private Integer repId;
    private Date allowedPurgeDt;
    private Integer createdBy;
    private Date createdDtm;
    private Integer modifiedBy;
    private Date modifiedDtm;
    private Integer uniqueCodeReceivingID;
}
