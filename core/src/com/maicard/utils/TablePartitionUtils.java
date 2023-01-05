package com.maicard.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.constants.Constants;

public class TablePartitionUtils {

	static final Logger logger = LoggerFactory.getLogger(TablePartitionUtils.class);


	public static String getTableMonth(String transactionId){
		
		if(StringUtils.isBlank(transactionId)) {
			logger.error("应用表月份时的订单号为空");
			return "";
		}
		
		
		String tableName = null;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date splitDate = DateUtils.truncate(DateUtils.addDays(new Date(), -1), Calendar.DAY_OF_MONTH);
		Date orderDate = null;
		try {
			orderDate = new SimpleDateFormat(Constants.orderIdDateFormat).parse(transactionId.substring(5, 5 + Constants.orderIdDateFormat.length()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(orderDate == null){
			logger.error("无法解析订单号[" + transactionId + "]的日期，无法转换表月份");
			return "";
		}
		logger.debug("订单号:{},日期是:{},分表时间是:{}",transactionId, StringTools.time2String(orderDate), StringTools.time2String(splitDate));
		if (!orderDate.before(splitDate)){		
			logger.debug("订单[" + transactionId + "]的日期位于分表时间点之后:" + StringTools.time2String(splitDate) + ",不需要换表月份");
			return "";
		} 
		 
		tableName = "_"	+ fmt.format(orderDate).substring(5, 7);
		 
		logger.debug("订单[" + transactionId + "]的表月份是:" + tableName);
		return tableName;

	}

	
	public static void main(String[] argv) {
		String id = "0112120011200029101";
		String m = getTableMonth(id);
		System.out.println(m);
	}
}
