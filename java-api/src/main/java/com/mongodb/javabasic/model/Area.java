package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class Area {
    private Integer areaId;
    private Integer casinoId;
    private String areaCode;
    private String areaType;
    private String areaName;
    private Integer markerTransferLocnId;
    private Date lastShiftDt;
    private Integer lastShift;
    private String sdsAreaId;
    private Integer isShiftAutoRoll;
    private Boolean isDefault;
    private Boolean isInactive;
    private Integer chipReqLocnId;
    private Integer createdBy;
    private Integer modifiedBy;
    private Date createdDtm;
    private Date modifiedDtm;
    private Integer dataRowVersion;
    private String localAreaCode;
}
