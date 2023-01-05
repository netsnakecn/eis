package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.Withdraw;
import com.maicard.money.entity.WithdrawMethod;
import com.maicard.security.entity.User;

public interface WithdrawService extends GlobalSyncService<Withdraw> {

	int delete(String  transactionId);

	Withdraw select(String transactionId);
	


	/**
	 * 开始一个提现
	 * @param withdraw
	 * @return
	 * @throws Exception 
	 */
	int begin(Withdraw withdraw, List<Withdraw> subWithdrawList) throws Exception;
		

	int isValidWithdraw(Withdraw withdraw);

	/**
	 * 返回一个支持的提现方法列表，不可返回null
	 * @param pay
	 * @param partner
	 * @return
	 */
	List<WithdrawMethod> getWithdrawMethod(Withdraw pay, User partner);

	int save(Withdraw withdraw);

	int updateWithdrawForManualOperate(CriteriaMap criteria);

	Withdraw queryByChannelRequestNo(String channelReqNo);
	
	public EisMessage end(int withdrawMethodId, String resultString, Object params);

	 

	

}
