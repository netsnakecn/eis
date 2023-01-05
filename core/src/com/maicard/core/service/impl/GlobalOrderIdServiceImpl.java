package com.maicard.core.service.impl;


import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.core.annotation.IgnoreDs;
import com.maicard.core.constants.Constants;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalOrderIdService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.misc.ThreadHolder;


@Service
@IgnoreDs
public class GlobalOrderIdServiceImpl extends BaseService implements GlobalOrderIdService {


	@Resource
	private ConfigService configService;

	@Resource
	private GlobalUniqueService globalUniqueService;

	private static int serverId;

	
	//唯一单号由三位服务器ID和两位交易类型ID作为前缀
	@Override
	public String generate(int transactionTypeId){
		return generate(transactionTypeId, new Date());
	}

	//唯一单号由三位服务器ID和两位交易类型ID作为前缀
	@Override
	public String generate(int transactionTypeId, Date orderDate){
		if(orderDate == null){
			orderDate = new Date();
		}
		if(serverId == 0) {
			serverId = configService.getServerId();
		}
		DecimalFormat serverFormat = new DecimalFormat("000");
		DecimalFormat transactionTypeFormat = new DecimalFormat("00");
		DecimalFormat randFormat = new DecimalFormat("00000");

		String prefix = serverFormat.format(serverId) + transactionTypeFormat.format(transactionTypeId);
		//String orderId = prefix + orderIdFormaterHolder.get().format(orderDate)  + (RandomUtils.nextInt(88888888) + 10000000);
		String orderId = prefix + ThreadHolder.orderIdFormaterHolder.get().format(orderDate)  + randFormat.format(globalUniqueService.incrOrderSequence(1));

		return orderId;
	}

	@Override
	public Date getDateByTransactionId(String transactionId){
		if(StringUtils.isBlank(transactionId) || transactionId.length() < 16){
			return null;
		}
		int length = Constants.ORDER_ID_TIME_FORMAT.length();
		try{
			return ThreadHolder.orderIdFormaterHolder.get().parse(transactionId.substring(5,5+length));
		}catch(Exception e){}
		return null;
	}
}
