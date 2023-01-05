package com.maicard.money.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;

@Data
public abstract class Stat extends BaseEntity{
 
	private static final long serialVersionUID = -5196422081779942173L;
	protected int successCount;
	protected int totalCount;
	protected double successMoney;	
	protected double totalMoney;				
	protected String statTime;	
	
	protected float commission;
	protected float profit;
	protected long inviter;
	
	protected Date createTime;
	

}
