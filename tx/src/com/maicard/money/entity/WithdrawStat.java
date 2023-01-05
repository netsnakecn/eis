package com.maicard.money.entity;

import lombok.Data;

@Data
public class WithdrawStat extends Stat{

	private static final long serialVersionUID = 1L;

	
	private long withdrawTypeId;
	
	private long withdrawMethodId;
	
	 
	private int bizType;
	
	
	
	@Override
	public String toString(){
		
			return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
					"(" + 
					"id=" +  id + 
					",withdrawTypeId=" + withdrawTypeId +
					",withdrawMethodId=" + withdrawMethodId +
					",totalCount=" + totalCount +
					",totalMoney=" + totalMoney +
					",successCount=" + successCount +
					",successMoney=" + successMoney +	
					",statTime=" + statTime +
					",inviter=" + inviter +
					")";
		
	}

	 

 
	
	




}
