package com.maicard.money.iface;

import com.maicard.money.entity.Pay;

public interface AfterPayProcessor {
	
	public int onAfterPay(Pay pay);

}
