package com.maicard.ws.service.impl;

import static com.maicard.ws.constants.WsConstants.ES_SESSION_MAP_NAME;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.utils.JsonUtils;
import com.maicard.ws.constants.SessionStatus;
import com.maicard.ws.constants.WsConstants;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.service.EsSessionService;
/**
 * 用于维护一个Map，保存本应用服务器所有在线的SocketSession及其对应的原生Session，如websocket或nio session<br>
 * 提供增加、删除和获取session的功能<br>
 * 并提供向这些session发送消息的功能<br>
 *
 *
 * @author NetSnake
 * @date 2016年11月11日
 *
 */
public class EsSessionServiceImpl extends BaseService implements EsSessionService{

	@Resource
	private ConfigService configService;
	@Resource
	private CenterDataService centerDataService;

	/*	@Autowired(required=false)
	private  SimpMessagingTemplate template;*/



	private static final int SEND_MESSAGE_RETRY = 3;
	private static final int MONITOR_MAX_IDEL = 10;


	@Override
	public EsSession get(String esSessionId){
		if(EsSessionContainer.localWsSessionMap.get(esSessionId) != null){
			logger.debug("在当前应用服务器中找到了指定的sessionId:" + esSessionId);
			return EsSessionContainer.localWsSessionMap.get(esSessionId);
		}
		EsSession o = centerDataService.getHmValue(ES_SESSION_MAP_NAME, esSessionId);
		if(o == null){
			logger.debug("在REDIS的表{}中找不到指定的sessionId:{}", ES_SESSION_MAP_NAME ,  esSessionId);
			return null;
		}

		return null;
	}


	@Override
	public int  put(EsSession esSession){
		Assert.notNull(esSession,"放入容器的wsSession不能为空");
		Assert.notNull(esSession.getEsSessionId(),"放入容器的wsSession,其sessionId不能为空");
		String esSessionId = esSession.getEsSessionId();
		try{
			centerDataService.setHmValue(ES_SESSION_MAP_NAME, esSessionId, esSession,-1);
			logger.info("向redis中放入esSession={},value={}" , esSessionId, JsonUtils.toStringFull(esSession));
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
		EsSessionContainer.localWsSessionMap.put(esSession.getEsSessionId(), esSession);
		return 1;

	}

	@Override 
	public int put(EsSession wsSession, Object nativeSession){
		WebSocketSession websocketSession = null;
		if(nativeSession == null){
			return 0;
		}
		this.put(wsSession);

		if(nativeSession instanceof WebSocketSession){
			websocketSession = (WebSocketSession)nativeSession;
			EsSessionContainer.localNativeSessionMap.put(websocketSession.getId(), websocketSession);
			return 1;
		} else {
			logger.error("错误的对象:{}" , nativeSession.getClass().getName());
			return 0;
		}

	}

	@Override
	public  int remove(String esSessionId){
		logger.debug("从redis中删除esSession:{}" , esSessionId);
		
		EsSession session = this.get(esSessionId);

		try{
			centerDataService.setHmValue(ES_SESSION_MAP_NAME, esSessionId, null,-1);
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
		if(session != null){
		//	EsSessionContainer.localWsSessionMap.remove(session.getNativeSessionId());
			EsSessionContainer.localWsSessionMap.remove(session.getEsSessionId());
			EsSessionContainer.localNativeSessionMap.remove(session.getNativeSessionId());
		}
		return 1;
	}

	@Override
	public  long count(CriteriaMap esSessionCriteria){
		long globalCount = centerDataService.countHm(ES_SESSION_MAP_NAME);
		logger.debug("当前应用服务器维护连接数:" , EsSessionContainer.localWsSessionMap.size() , ",全局活动连接数:" , globalCount);
		return 	globalCount;
	}

	/**
	 * 直接向本地Session发送消息
	 * @param uuid
	 * @param value
	 * @return 1发送成功，其他发送失败
	 */
	@Override
	public int sendLocalMessageDirect(long uuid, Object value, boolean forceSend){

		//从本地查找对应用户的Session
		if(EsSessionContainer.localWsSessionMap == null || EsSessionContainer.localWsSessionMap.size() < 1){
			return -1;
		}
		EsSession session = null;
		for(EsSession wsSession : EsSessionContainer.localWsSessionMap.values()){
			if(wsSession.getUser() != null && wsSession.getUser().getUuid() == uuid){
				session = wsSession;
				break;
			}
		}

		if(session == null){
			//logger.error("在全局WsSession池中找不到对应的session[" + sessionId + "]");
			return -1;
		} 
		int localServerId = configService.getServerId();
		if(session.getServerId() != localServerId){
			logger.info("尝试发送的消息:" + session.getServerId() + "]与本服务器ID:" + localServerId + "不一致");
			return -1;
		}
		if(!forceSend && session.getCurrentStatus() != SessionStatus.READY_FOR_MESSAGE.id){
			//还没有准备好接受消息
			logger.info("消息接收对应的玩家还没有准备好接受消息，session状态是:{}:",session.getCurrentStatus());
			return -1;
		}	
		//在本地查找Session
		if(!EsSessionContainer.localNativeSessionMap.containsKey(session.getNativeSessionId())){
			logger.info("在本地找到了EsSession:{},但是没有Native Session:{}对象",session.getEsSessionId() , session.getNativeSessionId());
			this.remove(session.getEsSessionId());

		}
		//在本应用服务器中找到了连接
		WebSocketSession s = EsSessionContainer.localNativeSessionMap.get(session.getNativeSessionId());
		if(s == null){
			logger.error("在本地WebSocketSession池中找到了对应的session[{}],但对应的session为空,删除", session.getEsSessionId());
			this.remove(session.getEsSessionId());
			return -1;
		} 
		if(!s.isOpen()){
			logger.error("在本地WebSocketSession池中找到了对应的session[{}]但对应的session不是开启状态,对应的用户是{}", session.getEsSessionId(),session.getUser().getUuid());
			//this.remove(session.getEsSessionId());
			return -1;
		}

		synchronized(s){
			try {
				//s.sendMessage(new TextMessage(value));
				TextMessage textMessage = new TextMessage(value.toString());
				s.sendMessage(textMessage);
				//logger.debug("=====>payload={},length={}",textMessage.getPayload(),textMessage.getPayloadLength());
				logger.info("直接向本地Session发送消息完成，摘要:{},时间戳:{}", (value.toString().length() > Constants.MESSAGE_BRIEF_LENGTH ? value.toString().substring(0, Constants.MESSAGE_BRIEF_LENGTH) + "......" : value), System.currentTimeMillis());
			} catch (IOException e) {
				logger.error("在直接向本地Session发送消息时出现异常，缓冲区大小:" + s.getTextMessageSizeLimit() + ":" + e.getMessage());
				e.printStackTrace();
				return -1;
			}
		}
		return 1;

	}



	@Override
	public  int sendMessage(ModelMap map, long... uuids){
		if(uuids == null || uuids.length < 1){
			logger.error("尝试发送的消息用户组为空");
			return -1;
		}

		//map.put("time",new Date().getTime()+"");
		String value = JsonUtils.toStringApi(map);

		if(value == null){
			logger.error("无法将消息[" + map + "]转换为JSON，放弃发送消息");
			return 0;
		}
		logger.info("向用户{}发送消息，摘要:{},时间戳:{}", Arrays.toString(uuids), (value.length() > Constants.MESSAGE_BRIEF_LENGTH ? value.substring(0, Constants.MESSAGE_BRIEF_LENGTH) + "......" : value), System.currentTimeMillis());
		for(long uuid : uuids){
			if(sendLocalMessageDirect(uuid,value,false) == 1){
				//本地直接发送成功
			} else {
				long score = new Date().getTime();
				addMessageToQueue(uuid, value, score);
			}
		}
		return 1;

	}
	
	@Override
	public  int sendMessageForce(ModelMap map, long... uuids){
		if(uuids == null || uuids.length < 1){
			return -1;
		}

		//map.put("time",new Date().getTime()+"");
		String value = JsonUtils.toStringApi(map);

		if(value == null){
			logger.error("无法将消息[" + map + "]转换为JSON，放弃发送消息");
			return 0;
		}
		for(long uuid : uuids){
			if(sendLocalMessageDirect(uuid,value, true) == 1){
				//本地直接发送成功
			} else {
				long score = new Date().getTime();
				addMessageToQueue(uuid, value, score);
			}
		}
		return 1;

	}
	
	

	@Override
	public void addMessageToQueue(long uuid, Object value, long score){
		String key = WsConstants.USER_MQ_INSTANCE_PREFIX + "#" + uuid;
		logger.info("向用户[" + uuid + "]消息队列放入消息:" + value + ",时间戳:" + score);
		centerDataService.addToZQueue(key, value.toString(), score);
	}
	@Override
	@Async
	public  void sendMessageDelay(int delaySec, ModelMap map, long... uuids) throws Exception{
		long score = 0;
		if(delaySec > 0){
			Thread.sleep(delaySec * 1000);
		} 
		score = new Date().getTime();
		//map.put("time",new Date().getTime()+"");
		String value = JsonUtils.toStringApi(map);
		for(long uuid : uuids){
			addMessageToQueue(uuid, value, score);
		}
	}


	// 发送消息到全世界，即所有用户
	@Override
	public  void sendWorldMessage(ModelMap map) throws Exception {

		List<EsSession> keys = centerDataService.getHmValues(ES_SESSION_MAP_NAME);
		if(keys == null || keys.size() < 1){
			logger.warn("当前没有任何活动连接，无法发送世界消息");
			return;
		}
		logger.debug("将向在线的" + keys.size() + "个连接发送世界消息");
		String value = JsonUtils.toStringApi(map);
		long score = new Date().getTime();

		for(EsSession  wsSession : keys){		
			if(wsSession == null){
				logger.warn("WsSession数据为空");
				continue;
			}
			if(wsSession.getUser() == null){
				logger.info("在线连接信息[" + wsSession + "]没有用户对象，忽略发送消息");
				continue;
			}
			addMessageToQueue(wsSession.getUser().getUuid(), value, score);


		}

	}

	//停止程序前调用，应当关闭本Tomcat所维护的所有活动连接
	@PreDestroy
	public void destroy(){
		//TODO
	}

	//清理连接队列中，所有不是当前连接但用户相同的连接，即清除指定用户的其他所有连接，只保留当前这一个
	@Override
	public void clearOldSessionForUser(EsSession wsSession) {
		logger.debug("进入到clearOldSessionForUser方法" + (wsSession == null));
		Assert.notNull(wsSession,"尝试清理用户连接的wsSession不能为空");
		if(wsSession.getUser() == null){
			logger.warn("尝试清理用户连接的wsSession[" + wsSession.getEsSessionId() + "]，其用户对象为空");
			return;
		}
		long uuid = wsSession.getUser().getUuid();
		logger.debug("进入到clearOldSessionForUser方法  going1" );
		Set<String> keys = centerDataService.getHmKeys(ES_SESSION_MAP_NAME);
		logger.debug("进入到clearOldSessionForUser方法  going2" );
		try{
			logger.debug("keys的长度为" + keys.size());
			int index = 0;
			for(Object sessionId : keys){
				index++;
				//logger.debug("进入到clearOldSessionForUser方法  index= " + index );
				EsSession wsSession2 = get(sessionId.toString());
				//logger.debug("进入到clearOldSessionForUser方法  going4" );
				if(wsSession2 != null && wsSession2.getUser() != null && wsSession2.getUser().getUuid() == uuid && !wsSession2.getEsSessionId().equals(wsSession.getEsSessionId())){
					logger.debug("移除用户[" + uuid + "]之前的连接:" + wsSession2.getEsSessionId());
					remove(wsSession2.getEsSessionId());
				}
			}
		}catch (Exception e) {
			logger.warn(e.getMessage());
		}
		
		logger.debug("进入到clearOldSessionForUser方法  over" );
		return;

	}
	

	@Override
	public ConcurrentHashMap<String,EsSession> getLocalSessionMap(){
		return EsSessionContainer.localWsSessionMap;
	}



	@Override
	@Async
	public void handlerMessageQueue(EsSession wsSession) throws Exception{
		Assert.notNull(wsSession, "消息队列的对应Session不能为空");
		//从REDIS中获取对应用户的消息，并发送给他
		if(!EsSessionContainer.localNativeSessionMap.containsKey(wsSession.getNativeSessionId())){
			logger.error("在本地WebSocketSession池中找不到指定的会话:" + wsSession.getEsSessionId());
			return;
		}
		//在本应用服务器中找到了连接
		WebSocketSession nativeSession = EsSessionContainer.localNativeSessionMap.get(wsSession.getNativeSessionId());
		if(wsSession.getUser() == null){
			logger.info("session[" + wsSession.getEsSessionId() + "]还没有登录用户，停止处理用户队列");
			return;
		}
		long uuid = wsSession.getUser().getUuid();
		String identify = new StringBuffer().append(uuid).append("#").append(Thread.currentThread().getName()).append("#").append(Thread.currentThread().getId()).append("#").append(new Date().getTime()).toString();

		String monitorKey = WsConstants.USER_MQ_LOCK_PREFIX + "#" + uuid;
		centerDataService.setForce(monitorKey, identify, MONITOR_MAX_IDEL);
		EsSessionContainer.userMessageHandlerMap.put(uuid, new Date().getTime());

		/*if(!setSucess){
			logger.info("session[" + wsSession.getSessionId() + "]已有用户消息监控程序:" + monitorKey);
			return;
		}*/
		logger.info("启动session[" + wsSession.getEsSessionId() + "]对应的用户消息监控程序:" + monitorKey);

		while(true){
			if(nativeSession == null){
				logger.error("在本地WebSocketSession池中找到了对应的session[" + wsSession.getEsSessionId() + "],但对应的session为空");
				this.remove(wsSession.getEsSessionId());
				centerDataService.delete(monitorKey);
				return;
			} 
			if(!nativeSession.isOpen()){
				logger.warn("在本地WebSocketSession池中找到了对应的session[" + wsSession.getEsSessionId() + "],但对应的session不是开启状态");
				//centerDataService.delete(monitorKey);
				return;
			}
			if(wsSession.getCurrentStatus() != SessionStatus.READY_FOR_MESSAGE.id){
				//还没有准备好接受消息
				Thread.sleep(WsConstants.USER_MQ_READ_INTERVAL);
				continue;
			}
			//获取监控数据，如果跟当前线程不一致，则说明是由其他线程接管了，本线程退出监控
			String existValue = centerDataService.get(monitorKey);
			if(existValue != null && !existValue.equals(identify)){
				logger.info("用户消息队列:{}已经由线程:{}监控，本监控线程{}退出" ,monitorKey, existValue , identify );
				return;
			}
			centerDataService.setForce(monitorKey, identify, MONITOR_MAX_IDEL);
			EsSessionContainer.userMessageHandlerMap.put(uuid, new Date().getTime());
			Thread.sleep(WsConstants.USER_MQ_READ_INTERVAL);




			String key = new StringBuffer().append(WsConstants.USER_MQ_INSTANCE_PREFIX).append("#").append(wsSession.getUser().getUuid()).toString();
			//String value = centerDataService.pushFromZQueue(key, false);
			//每次取1条
			Set<String> values = centerDataService.pushSetFromZQueue(key, false, 0, 1);

			int length = values.size();

			if(length < 1){
				logger.debug("消息队列:{}中没有任何数据", key);
				//没有得到任何消息
				continue;
			}
			int m = 0;
			for(String value : values){
				logger.info("从消息队列中获取到了{}条消息,向session[{}/{}]发送第{}条,消息长度:{}，时间戳:{}", length, wsSession.getEsSessionId(), wsSession.getNativeSessionId(), (m+1), value.length(), System.currentTimeMillis());
				for(int i = 0; i < SEND_MESSAGE_RETRY; i++){
					try{
						synchronized(nativeSession){
							nativeSession.sendMessage(new TextMessage(value));
						}
						//logger.debug("从消息队列中获取到了最早的消息，第" + (i+1) + "次向session[" + wsSession + "]发送消息:" + value + "，发送完成");
						break;
					}catch(IllegalStateException ie){
						logger.error("把消息队列发往Session时出现异常，缓冲区大小:" + nativeSession.getTextMessageSizeLimit() + ":" + ie.getMessage());
						ie.printStackTrace();
						Thread.sleep(100);
					}
				}
				m++;
			}


		}
	}

	@Override
	public void clearUserMessageQueue(EsSession wsSession) {
		String key = new StringBuffer().append(WsConstants.USER_MQ_INSTANCE_PREFIX).append("#").append(wsSession.getUser().getUuid()).toString();
		centerDataService.delete(key);
	}

	@Override
	public List<EsSession> listOnPage(CriteriaMap esSessionCriteria) {
		List<EsSession> esSessionList = centerDataService.getHmValues(ES_SESSION_MAP_NAME);
		if(esSessionList == null){
			return Collections.emptyList();
		}
		return esSessionList;
	}


	@Override
	public EsSession get(long receiverId) {
		// TODO Auto-generated method stub
		return null;
	}

}
