package com.mongodb.javabasic.model;

import java.util.Date;


import lombok.Data;

@Data
public class PlayerCard {
    private Integer playerId;
    private Integer acct;
    private Integer lastLocnId;
    private Boolean isCardIn;
    private Integer cardId;
    private Integer cardCount;
    private Boolean isInactive;
    private String track1Data;
    private String track2Data;
    private String track3Data;
    private Integer createdBy;
    private Integer createdCasinoId;
    private String createdComputerName;
    private Integer modifiedBy;
    private Integer modifiedCasinoId;
    private String modifiedComputerName;
    private Date createdDtm;
    private Date modifiedDtm;
    private Integer dataRowVersion;
    private Integer acctInt;
    private String playStatus;
    private Integer lastGamingLocn;
    private Integer cMSAccountId;
    private Integer cmsid;
    private Integer cardSequenceNum;
}
