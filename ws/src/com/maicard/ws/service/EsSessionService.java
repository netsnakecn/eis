package com.maicard.ws.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.ModelMap;

import com.maicard.base.CriteriaMap;
import com.maicard.ws.entity.EsSession;
 
public interface EsSessionService {

	void sendWorldMessage(ModelMap map) throws Exception;

	/**
	 * 发送消息给指定的用户，如果用户Session不是准备好接收消息，则该消息不会发送
	 * 
	 *
	 * @author GHOST
	 * @date 2018-01-02
	 */
	int sendMessage(ModelMap map, long... uuids);
	
	/**
	 * 不考虑用户Session状态，强制发送消息给用户
	 * 
	 *
	 * @author GHOST
	 * @date 2018-01-02
	 */
	int sendMessageForce(ModelMap map, long... uuids);
	
	/**
	 * 直接向本地Session发送消息
	 * @param uuid
	 * @param value
	 * @return 1发送成功，其他发送失败
	 */
	int sendLocalMessageDirect(long uuid, Object value, boolean forceSend);



	EsSession get(String sessionId);
	
	/**
	 * 根据用户UUID返回他的EsSession
	 * 
	 *
	 * @author NetSnake
	 * @date 2018-05-22
	 */
	EsSession get(long uuid);


	int put(EsSession esSession);

	int remove(String sessionId);

	long count(CriteriaMap esSessionCriteria);


	//清理连接队列中，所有不是当前连接但用户相同的连接，即清除指定用户的其他所有连接，只保留当前这一个
	void clearOldSessionForUser(EsSession socketSession);

	//得到本Tomcat容器中的所有活动连接
	ConcurrentHashMap<String, EsSession> getLocalSessionMap();

	int put(EsSession socketSession, Object nativeSession);

	/**
	 * 异步发送消息，并先延迟
	 * @param delaySec
	 * @param map
	 * @param uuids
	 * @throws Exception
	 */
	@Async
	void sendMessageDelay(int delaySec, ModelMap map, long... uuids) throws Exception;

	@Async
	void handlerMessageQueue(EsSession socketSession) throws  Exception;

	void clearUserMessageQueue(EsSession socketSession);

	List<EsSession> listOnPage(CriteriaMap esSessionCriteria);

	void addMessageToQueue(long uuid, Object value, long score);






}
