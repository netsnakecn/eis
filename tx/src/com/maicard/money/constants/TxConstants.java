package com.maicard.money.constants;

public class TxConstants {

    public static final String DEFAULT_AVAILABLE_COUNT = "DEFAULT_AVAILABLE_COUNT" ;
    public static final String STOCK_MODE = "STOCK_MODE" ;
    public static int payMoveToHistoryDay = 2;
	
	public static final int notifySendRetry = 5;
	
	public static final int notifySendRetryInterval = 10;
	
	public static final int txTtl = 3600 * 24;
	
	public static final String CACHE_STOCK_PREFIX = "GOODS_STOCK";

	public static final String DEFAULT_PAY_NOTIFY_TEMPLATE = "${hostUrl}/payNotify/${transactionId}.json";
	public static final String DEFAULT_REFUND_NOTIFY_TEMPLATE = "${hostUrl}/refundNotify/${transactionId}.json";


}
