package com.maicard.ws.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

import com.maicard.ws.entity.EsSession;



/**
 * 用来存放一些静态缓存，供多个service调用，防止service之间耦合
 *
 *
 * @author GHOST
 * @date 2017-12-20
 */
public  class EsSessionContainer {
	

	/**
	 * 存放Eis Session的数据
	 */
	public static ConcurrentHashMap<String,EsSession> localWsSessionMap = new ConcurrentHashMap<String,EsSession>();

	/**
	 * 存放Tomcat的Websocket Session的数据
	 */
	public static ConcurrentHashMap<String,WebSocketSession> localNativeSessionMap = new ConcurrentHashMap<String,WebSocketSession>();

	
	/**
	 * 存放用户消息监控线程的最新一次更新
	 */
	public static Map<Long,Long> userMessageHandlerMap = new ConcurrentHashMap<Long, Long>();

}
