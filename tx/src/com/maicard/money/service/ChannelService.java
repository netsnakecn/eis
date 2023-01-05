package com.maicard.money.service;

import com.maicard.money.entity.Pay;
import com.maicard.money.entity.PayChannelMechInfo;
import com.maicard.money.entity.Withdraw;

public interface ChannelService {

	public PayChannelMechInfo  getChannelInfo(Pay pay);
	
	public PayChannelMechInfo  getChannelInfo(Withdraw withdraw);

}
