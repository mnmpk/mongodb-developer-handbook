package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class Casino {
    private Integer casinoId;
    private String casinoCode;
    private String csinoName;
    private String addr1;
    private String addr2;
    private String city;
    private Integer stateId;
    private String stateName;
    private Integer cuntryId;
    private String countryName;
    private Integer postalCodeId;
    private String postalCode;
    private String tel1Type;
    private String tel1;
    private String tel2Type;
    private String tel2;
    private Integer firstShiftStartMinutes;
    private Integer shiftLengthHours;
    private Integer maxShift;
    private String sdsAreaID;
    private String sdsTemplateLocnID;
    private String taxId;
    private String stateTaxId;
    private String defaultBankTransitDays;
    private Boolean isInactive;
    private Integer createdBy;
    private Integer modifiedBy;
    private Boolean addressValidated;
    private Boolean addressOverriden;
    private String addressOverrideReason;
    private Boolean batchValidationStatus;
    private Boolean batchAddressValidated;
    private String batchAddressCorrectionCode;
    private Integer oldAddressId;
    private Integer shiftEndPriorMinutes;
    private Date addressValidatedDtm;
    private Date createdDtm;
    private Date modifiedDtm;
    private Integer siteId;
    private String timeZone;
    private Integer accessLevelId;
    private Integer dataRowVersion;
    private String casinoIdPrefix;
    private Integer accountPrefixLength;
}
