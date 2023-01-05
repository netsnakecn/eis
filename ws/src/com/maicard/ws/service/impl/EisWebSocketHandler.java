package com.maicard.ws.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.utils.HttpUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.StringTools;
import com.maicard.ws.annotation.ProcessWsMessageOperate;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.iface.WsMessageListener;
import com.maicard.ws.service.EsSessionService; 

@Service
public class EisWebSocketHandler extends TextWebSocketHandler {

	protected final Logger logger = LoggerFactory.getLogger(getClass());


	@Autowired
	private ApplicationContextService applicationContextService;

	@Autowired
	private EsSessionService wsSessionService;

	ObjectMapper om = JsonUtils.getNoDefaultValueInstance();


	/**
	 * 动作与处理该动作的所有bean的一个集合<br>
	 * 当收到某个动作时，即调用对应Set中的所有bean处理具体业务
	 */
	private static ConcurrentHashMap<String, Set<WsMessageListener>> handlerMap = null;



	/*public EisWebSocketHandler(SimpMessagingTemplate template){

	}*/


	/*	@OnMessage
	public void onMessage(Session session, String message) throws Exception {
		if(logger.isDebugEnabled()){
			if(logger.isDebugEnabled())logger.debug("获取到WS消息:" +  message);
		}
		Map<String,String> params = HttpUtils.getRequestDataMap(message);
		String action = params.get("action");
		if(action == null){
			logger.error("消息中未提交操作代码，消息正文:" + message);
			session.getBasicRemote().sendText(om.writeValueAsString(new EisMessage(EisError.requiredDataNotFound.id,"请提交操作代码")));		
			//session.close();
		}

		// template.convertAndSend("/topic/getLog", text); // 这里用于广播
	}*/




	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message){

		String text = message.getPayload() != null ? message.getPayload().trim() : null;
		if(logger.isDebugEnabled()){
			if(logger.isDebugEnabled())logger.debug("获取到WS客户端:" + session.getId() + ",来自:" + session.getLocalAddress().getHostString() +  "的消息:" +  message.getPayload());
			//siteDomainRelationService.getByHostName(hostName);
		}
		//session.
		/*for(String key : session.getAttributes().keySet()){
			if(logger.isDebugEnabled())logger.debug("当前session属性:" + key + "=>" + session.getAttributes().get(key));
		}*/
		//if(logger.isDebugEnabled())logger.debug("当前线程数:" + applicationContextService.getThreadCount());

		Map<String,String> params = null;
		if(text.startsWith("{") && text.endsWith("}") || ( text.startsWith("[") && text.endsWith("]"))) {
			try {
				params = JsonUtils.getInstance().readValue(text, Map.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(params == null) {
			params = HttpUtils.getRequestDataMap(text);
		}

		_handlerMessage(session, params);


	}


	protected void _handlerMessage(WebSocketSession session, Map<String,String>params) {


		EsSession esSession = null;
		Object esSessionId = session.getAttributes().get("esSessionId");
		if(esSessionId != null){
			esSession = wsSessionService.get(esSessionId.toString());
		}
		if(esSession == null){
			Object o = session.getAttributes().get("ownerId");
			if(logger.isDebugEnabled())logger.debug("从附加属性中获取ownerId=" + o);
			long ownerId = 0;
			if(o != null && o instanceof Long){
				ownerId = (long)o;
			}
			o = session.getAttributes().get("serverId");
			if(logger.isDebugEnabled())logger.debug("从附加属性中获取serverId=" + o);
			int serverId = 0;
			if(o != null && o instanceof Integer){
				serverId = (int)o;
			}
			String newEsSessionId = esSessionId == null ? UUID.randomUUID().toString() : esSessionId.toString();
			esSession = new EsSession(newEsSessionId, session.getId(),ownerId);
			if(esSessionId == null){
				session.getAttributes().put("esSessionId", newEsSessionId);
			}
			esSession.setServerId(serverId);

		}
		Object o = session.getAttributes().get("clientIp");
		if(o != null){
			esSession.setClientIp(o.toString());
		}
		esSession.getExt().putAll(session.getAttributes());
		o = session.getAttributes().get("serverURL");
		if(logger.isDebugEnabled())logger.debug("从附加属性中获取serverId=" + o);
		if(o != null){
			esSession.getExt().put("serverURL", o.toString());
		}
		if(esSession.getLastSaveTime() == null || (new Date().getTime() - esSession.getLastSaveTime().getTime()) / 1000 > Constants.SESSION_SAVE_INTERVAL){
			esSession.setLastSaveTime(new Date());
			wsSessionService.put(esSession,session);
			if(logger.isDebugEnabled())logger.debug("向系统中放入EsSession对象:" + esSession.getEsSessionId() + ",nativeSessionId=" + session.getId() + ",serverId=" + esSession.getServerId());

		} else {
			logger.debug("EsSession对象的上次存储时间是:{},本次忽略",StringTools.time2String(esSession.getLastSaveTime()));
		}


		ModelMap map = new ModelMap();
		//map.put("operate", Operate.notify.toString());

		String action = params.get("action");
		if(action == null){
			logger.error("消息中未提交操作代码action");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id,"请提交操作代码"));
			synchronized(session){
				try {
					session.sendMessage(new TextMessage(om.writeValueAsString(map)));
				} catch (IOException e) {
					logger.error("无法向客户端发送消息因为出现错误:" + e.getMessage());
				}
			}
			return;
		}

		action = action.trim();


		Date beginTime = new Date();

		if(handlerMap == null){
			initHandlerMap();
		}
		if(handlerMap == null || handlerMap.size()  < 1){
			logger.error("系统中没有类型为[" + WsMessageListener.class.getName() + "]的bean");
			return;
		}
		if(handlerMap.get(action) == null){
			logger.warn("当前系统中没有注册任何处理action=" + action + "的bean");
			return;
		}
		if(logger.isDebugEnabled()){
			if(logger.isDebugEnabled())logger.debug("系统注册了" + handlerMap.get(action).size() + "个服务来处理action=" + action);
		}
		for(WsMessageListener bean : handlerMap.get(action)){
			if(logger.isDebugEnabled())logger.debug("把消息交给类[" + bean.getClass().getName() + "]处理WS操作:" + action);
			try {
				bean.onWsMessage(esSession, params, map);
			} catch (Exception e) {
				logger.error("服务[" + bean.getClass().getName() + "]无法处理WS消息，服务抛出异常:" + e.getMessage());
				map.put("message", EisMessage.error(EisError.systemException.id,"系统异常"));
				e.printStackTrace();
			}
		}

		if(map.isEmpty()){
			if(logger.isDebugEnabled())logger.debug("消息处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,但返回数据MAP为空，不发送数据");
		} else if(!session.isOpen()){
			if(logger.isDebugEnabled())logger.debug("消息处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,但Session已关闭");
		} else {
			if(logger.isDebugEnabled())logger.debug("消息处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,向Session发送消息");
			sendMessage(session, map);
			//wsSessionService.sendMessage(map,session.getId());
		}

		//FIXME 为啥每次要放入呢？
		//wsSessionService.put(esSession,session);

		return;
	}

	private synchronized void  sendMessage(WebSocketSession session, ModelMap map) {
		try {
			session.sendMessage(new TextMessage(om.writeValueAsString(map)));
			//	if(logger.isDebugEnabled())logger.debug("ES消息[" + text + "]处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,已完成向Session发送消息");
		} catch (IOException e) {
			logger.error("无法向客户端发送消息因为出现错误:" + e.getMessage());
		}
	}

	private synchronized void initHandlerMap() {
		ApplicationContext applicationContext = applicationContextService.getApplicationContext();

		Map<String,WsMessageListener>map  = applicationContext.getBeansOfType(WsMessageListener.class);
		if(map == null || map.size() < 1){
			logger.error("系统中没有类型为[" + WsMessageListener.class.getName() + "]的bean");
			return;
		}
		for(String beanName : map.keySet()){

			ProcessWsMessageOperate processObjectAnnotation = applicationContext.findAnnotationOnBean(beanName, ProcessWsMessageOperate.class);

			if(processObjectAnnotation == null){
				if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug(beanName + "未声明ProcessWsMessageOperate注解");
				}
				continue;
			}
			if(processObjectAnnotation.value() == null || processObjectAnnotation.value().length < 1){
				if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug(beanName + "的ProcessWsMessageOperate注解内容为空");
				}
				continue;
			}
			if(handlerMap == null){
				handlerMap = new ConcurrentHashMap<String, Set<WsMessageListener>>();
			}

			for(String value : processObjectAnnotation.value()){
				if(StringUtils.isBlank(value)){
					if(logger.isDebugEnabled())logger.debug("忽略[" + beanName + "]类的注解ProcessWsMessageOperate中的空指令");
					continue;
				}
				if(handlerMap.get(value) == null){
					handlerMap.put(value, new HashSet<WsMessageListener>());
				}
				handlerMap.get(value).add(map.get(beanName));
				/*if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug("把bean" + beanName + "作为操作[" + value + "]的处理者");
				}*/
			}

		}
		logger.info("系统WS处理器初始化完成，共注册:" + handlerMap.size() + "个指令");


	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		if(logger.isDebugEnabled())logger.debug("WS session[" + session.getId() + "]连接已关闭，向系统消息总线发送DISCONNECT消息");
		//发送用户已断线的消息
		Map<String,String> params = new HashMap<String,String>();
		params.put("action","DISCONNECT");
		Object o = session.getAttributes().get("esSessionId");
		if(o != null){
			wsSessionService.remove(o.toString());
		} else {
			if(logger.isDebugEnabled())logger.debug("WS session[" + session.getId() + "]连接已为空或找不到其中的esSessionId扩展数据，不执行删除");
		}
		params.put("esSessionId", o.toString());
		_handlerMessage(session,params);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		if(logger.isDebugEnabled())logger.debug("WS session[" + session.getId() + "]建立了连接");

		super.afterConnectionEstablished(session);
	}


}
