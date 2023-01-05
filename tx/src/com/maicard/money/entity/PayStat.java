package com.maicard.money.entity;


import lombok.Data;

@Data
public class PayStat extends Stat{

	private static final long serialVersionUID = 1L;

	
	private long payTypeId;
	
	private long payMethodId;

	private long payToAccount;
	
	
	/**
	 * 付款卡类型
	 */
	private String payCardType;
	
	
	
	@Override
	public String toString(){
		
			return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
					"(" + 
					"id=" +  id + 
					",payTypeId=" + payTypeId +
					",payMethodId=" + payMethodId +
					",totalCount=" + totalCount +
					",totalMoney=" + totalMoney +
					",successCount=" + successCount +
					",successMoney=" + successMoney +	
					",statTime=" + statTime +
					",inviter=" + inviter +
					")";
		
	}

	 

	
	




}
