package com.maicard.ws.constants;

public class WsConstants {
	/**
	 * 存放EisSocket的缓存名称
	 */

	public static final String ES_SESSION_MAP_NAME = "ES_SESSION_MAP";
	
	
	/**
	 * 这个毫秒数内没有任何动作并且没有登录的Session应当被清除
	 */
	public static final long ES_SESSION_NO_USER_IDLE_MS = 300 * 1000;
	
	public static final long USER_MQ_READ_INTERVAL = 300;		//用户队列的消息获取间隔时间为300毫秒
	


	public static final String USER_MQ_INSTANCE_PREFIX = "USER_MQ";

	public static final String USER_MQ_LOCK_PREFIX = "USER_MQ_LOCK";


	

	
}
