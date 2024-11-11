package com.mongodb.javabasic.model;

import java.util.Date;

import lombok.Data;

@Data
public class Dept {
	private Integer deptId;
	private String deptCode;
	private String deptName;
	private Boolean isDefault;
	private String casinoWinCalcMethod;
	private Boolean isRating;
	private Boolean isGaming;
	private Boolean isSystemUse;
	private Boolean isInactive;
	private Integer createdBy;
	private Integer modifiedBy;
	private Date createdDtm;
	private Date modifiedDtm;
	private Integer dataRowVersion;
	private Integer casinoId;
	private Integer autoId;
}
