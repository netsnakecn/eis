package com.maicard.money.service;

import java.util.List;
import java.util.Map;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.EisMessage;
import com.maicard.money.constants.MoneyType;
import com.maicard.money.entity.Pay;
import com.maicard.money.iface.PayProcessor;
import com.maicard.security.entity.User;
import com.maicard.tx.entity.Order;

import javax.servlet.http.HttpServletRequest;

public interface PayService extends GlobalSyncService<Pay> {

	EisMessage beginPay(Object order,  User frontUser, HttpServletRequest request);

	Pay select(String transactionId);
	

	EisMessage end(String tid, String resultString, Object params) throws Exception;
	

	int countByPartner(CriteriaMap payCriteria);

	EisMessage begin(Pay pay);
		
	EisMessage end(Pay pay) throws Exception;


	int refund(Pay orig, Pay refund);

	Map<String, String> generateClientResponseMap(Pay pay);

	PayProcessor getProcessor(Pay pay);

	List<Pay> lock(CriteriaMap payCriteria);

	Pay lock(Pay pay);


    void createRefundNotifyUrl(Pay pay, HttpServletRequest request);

    void createNotifyUrl(Pay pay, HttpServletRequest request);
}
