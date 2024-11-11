package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class Game {
    private Integer gameId;
    private String gameCode;
    private String gameName;
    private Integer minObservationMinutes;
    private Double defaultAvgBet;
    private Boolean isDefaultDenom;
    private Boolean isInactive;
    private Integer numberOfAvgBets;
    private Integer createdBy;
    private Integer modifiedBy;
    private Boolean isEnableBackTrackRestriction;
    private Boolean isEnableFwdTRackRestriction;
    private Date createdDtm;
    private Date modifiedDtm;
    private Integer dataRowVersion;
    private Double defaultAvgBetMultiplier;
}
