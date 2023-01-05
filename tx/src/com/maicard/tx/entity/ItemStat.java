package com.maicard.tx.entity;

import com.maicard.money.entity.Stat;

public class ItemStat extends Stat{

	private static final long serialVersionUID = 1L;

	
	
	private long productId;
	
	 
	
	
	@Override
	public String toString(){
		
			return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
					"(" + 
					"id=" +  id + 
					",productId=" + productId +
					",totalCount=" + totalCount +
					",totalMoney=" + totalMoney +
					",successCount=" + successCount +
					",successMoney=" + successMoney +	
					",statTime=" + statTime +
					",inviter=" + inviter +
					")";
		
	}

	   


	public long getProductId() {
		return productId;
	}



	public void setProductId(long productId) {
		this.productId = productId;
	}
	
	




}
