package com.maicard.misc;

import java.text.SimpleDateFormat;

import com.maicard.core.constants.Constants;

public class ThreadHolder {

	public static ThreadLocal<SimpleDateFormat> orderIdFormaterHolder 	= new ThreadLocal<SimpleDateFormat>() {
		public SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Constants.ORDER_ID_TIME_FORMAT);
		}
	};
	
	
	public static ThreadLocal<SimpleDateFormat> defaultTimeFormatterHolder = new ThreadLocal<SimpleDateFormat>() {
		public SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);
		}
	};
	
	public static ThreadLocal<SimpleDateFormat> fileTimeFormaterHolder = new ThreadLocal<SimpleDateFormat>() {
		public SimpleDateFormat initialValue() {
			return new SimpleDateFormat(Constants.FILE_NAME_DATE_FORMAT);
		}
	};
}
