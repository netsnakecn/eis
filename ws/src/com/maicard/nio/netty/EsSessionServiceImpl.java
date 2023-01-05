package com.maicard.nio.netty;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.utils.JsonUtils;
import com.maicard.ws.constants.WsConstants;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.service.EsSessionService;
import static com.maicard.ws.constants.WsConstants.ES_SESSION_MAP_NAME;

import io.netty.channel.ChannelHandlerContext;

/**
 * 用于维护一个Map，保存本应用服务器所有在线的Netty Session、WsSession
 * 提供增加、删除和获取session的功能
 * 并提供向这些session发送消息的功能
 *
 *
 * @author NetSnake
 * @date 2016年11月11日
 *
 */

@Service
public class EsSessionServiceImpl extends BaseService implements EsSessionService{

	@Resource
	private ConfigService configService;
	@Resource
	private CenterDataService centerDataService;


	private static ConcurrentHashMap<String,EsSession> localEsSessionMap = new ConcurrentHashMap<String,EsSession>();

	private static ConcurrentHashMap<String,ChannelHandlerContext> localNativeSessionMap = new ConcurrentHashMap<String,ChannelHandlerContext>();

	//private static final int SEND_MESSAGE_RETRY = 3;
	private static final int MONITOR_MAX_IDEL = 10;


	@Override
	public EsSession get(String sessionId){
		if(localEsSessionMap.get(sessionId) != null){
			logger.debug("在当前应用服务器中找到了指定的sessionId:" + sessionId);
			return localEsSessionMap.get(sessionId);
		}
		EsSession o = centerDataService.getHmValue(ES_SESSION_MAP_NAME, sessionId);
		if(o == null){
			logger.debug("在REDIS的ES_SESSION_MAP_NAME表中找不到指定的sessionId:" + sessionId);
			return null;
		}

		return null;
	}


	@Override
	public int  put(EsSession wsSession){
		Assert.notNull(wsSession,"放入容器的wsSession不能为空");
		Assert.notNull(wsSession.getEsSessionId(),"放入容器的wsSession,其sessionId不能为空");
		String sessionId = wsSession.getEsSessionId();
		try{
			centerDataService.setHmValue(ES_SESSION_MAP_NAME, sessionId, wsSession,-1);
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
		localEsSessionMap.put(wsSession.getEsSessionId(), wsSession);
		return 1;

	}

	@Override 
	public int put(EsSession wsSession, Object nativeSession){
		ChannelHandlerContext websocketSession = null;
		if(nativeSession == null){
			return 0;
		}
		if(nativeSession instanceof ChannelHandlerContext){
			websocketSession = (ChannelHandlerContext)nativeSession;
			this.put(wsSession);
			localNativeSessionMap.put(websocketSession.channel().id().asShortText(), websocketSession);
			return 1;
		} else {
			logger.error("错误的对象:" + nativeSession.getClass().getName());
			return 0;
		}

	}

	@Override
	public  int remove(String sessionId){
		logger.debug("删除session:" + sessionId);

		try{
			centerDataService.setHmValue(ES_SESSION_MAP_NAME, sessionId, null,-1);
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}

		localEsSessionMap.remove(sessionId);
		return 1;
	}

	@Override
	public  long count(CriteriaMap esSessionCriteri){
		long globalCount = centerDataService.countHm(ES_SESSION_MAP_NAME);
		logger.debug("当前应用服务器维护连接数:" + localEsSessionMap.size() + ",全局活动连接数:" + globalCount);
		return 	globalCount;
	}

	/*	private  int sendMessage(String sessionId, ModelMap map) throws Exception{
		if(template == null){
			WsSession wsSession = get(sessionId);
			if(wsSession == null){
				logger.error("在全局WsSession池中找不到对应的session[" + sessionId + "]");
				return -1;
			} 
			int localServerId = configService.getServerId();
			logger.debug("尝试发送的消息[sessionId=" + sessionId + ",serverId=" + wsSession.getServerId() + "],本应用服务器serverId=" + localServerId);
			if(wsSession.getServerId() == localServerId){
				//在本地查找Session
				if(localSessionMap.containsKey(sessionId)){
					//在本应用服务器中找到了连接
					WebSocketSession s = localSessionMap.get(sessionId);
					if(s == null){
						logger.error("在本地WebSocketSession池中找到了对应的session[" + sessionId + "],但对应的session为空");
						return -1;
					} 
					if(!s.isOpen()){
						logger.error("在本地WebSocketSession池中找到了对应的session[" + sessionId + "],但对应的session不是开启状态");
						return -1;
					}

					String text = om.writeValueAsString(map);
					logger.debug("向session[" + wsSession + "]发送消息:" + text);
					synchronized(s){
						s.sendMessage(new TextMessage(text));
					}
					return 1;


				} else {
					logger.error("在本地WebSocketSession池中找不到对应的session[" + sessionId + "]");
				}
				return -1;

			} else {

				//不是本地的Session，发送世界消息
				EisMessage eisMessage = new EisMessage();
				eisMessage.setAttachment(new HashMap<String,Object>());
				WsSession wsMsg = new WsSession();
				wsMsg.setSessionId(sessionId);
				wsMsg.setServerId(wsSession.getServerId());
				wsMsg.setPayload(om.writeValueAsString(map));
				eisMessage.getAttachment().put("wsSession", wsMsg);
				eisMessage.setObjectType(ObjectType.wsMessage.getCode());
				messageService.send(null, eisMessage);
			}


		} else {
			String destination = WebSocketConstants.USER_DESTINATION + "/message-user" + sessionId;
			String text = om.writeValueAsString(map);
			logger.debug("向session[" + sessionId + "]发送Stomp消息:" + text);
			template.convertAndSend(destination, text);
		}
		return 1;
	}
	 */


	@Override
	public  int sendMessage(ModelMap map, long... uuids){
		if(uuids == null || uuids.length < 1){
			return -1;
		}

		long score = new Date().getTime();
		String value = JsonUtils.toStringApi(map);
		if(value == null){
			logger.error("无法将消息[" + map + "]转换为JSON，放弃发送消息");
			return 0;
		}
		for(long uuid : uuids){
			addMessageToQueue(uuid, value, score);
		}
		return 1;

	}

	@Override
	public void addMessageToQueue(long uuid, Object value, long score){
		String key = WsConstants.USER_MQ_INSTANCE_PREFIX + "#" + uuid;
		if(logger.isDebugEnabled())logger.debug("向用户[" + uuid + "]消息队列放入消息:" + value + ",时间戳:" + score);
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
		Assert.notNull(wsSession,"尝试清理用户连接的wsSession不能为空");
		if(wsSession.getUser() == null){
			logger.warn("尝试清理用户连接的wsSession[" + wsSession.getEsSessionId() + "]，其用户对象为空");
			return;
		}
		long uuid = wsSession.getUser().getUuid();
			Set<String> keys = centerDataService.getHmKeys(ES_SESSION_MAP_NAME);

		for(Object sessionId : keys){
			EsSession wsSession2 = get(sessionId.toString());
			if(wsSession2 != null && wsSession2.getUser() != null && wsSession2.getUser().getUuid() == uuid && !wsSession2.getEsSessionId().equals(wsSession.getEsSessionId())){
				logger.debug("移除用户[" + uuid + "]之前的连接:" + wsSession2.getEsSessionId());
				remove(wsSession2.getEsSessionId());
			}
		}

		return;

	}

	@Override
	public ConcurrentHashMap<String,EsSession> getLocalSessionMap(){
		return localEsSessionMap;
	}


	@Override
	@Async
	public void handlerMessageQueue(EsSession wsSession) throws Exception{
		Assert.notNull(wsSession, "消息队列的对应Session不能为空");
		//从REDIS中获取对应用户的消息，并发送给他
		if(!localNativeSessionMap.containsKey(wsSession.getEsSessionId())){
			logger.error("在本地WebSocketSession池中找不到指定的会话:" + wsSession.getEsSessionId());
			return;
		}
		String identify = Thread.currentThread().getName() + "#" + Thread.currentThread().getId() + "#" + new Date().getTime();
		//在本应用服务器中找到了连接
		ChannelHandlerContext s = localNativeSessionMap.get(wsSession.getEsSessionId());
		if(wsSession.getUser() == null){
			logger.info("session[" + wsSession.getEsSessionId() + "]还没有登录用户，停止处理用户队列");
			return;
		}
		String monitorKey = WsConstants.USER_MQ_LOCK_PREFIX + "#" + wsSession.getUser().getUuid();
		centerDataService.setForce(monitorKey, identify, MONITOR_MAX_IDEL);
		/*if(!setSucess){
			logger.info("session[" + wsSession.getEsSessionId() + "]已有用户消息监控程序:" + monitorKey);
			return;
		}*/
		logger.info("启动session[" + wsSession.getEsSessionId() + "]对应的用户消息监控程序:" + monitorKey);

		while(true){
			if(s == null){
				logger.error("在本地WebSocketSession池中找到了对应的session[" + wsSession.getEsSessionId() + "],但对应的session为空");
				this.remove(wsSession.getEsSessionId());
				centerDataService.delete(monitorKey);
				return;
			} 
			if(!s.channel().isOpen()){
				logger.warn("在本地WebSocketSession池中找到了对应的session[" + wsSession.getEsSessionId() + "],但对应的session不是开启状态");
				centerDataService.delete(monitorKey);
				return;
			}
			//获取监控数据，如果跟当前线程不一致，则说明是由其他线程接管了，本线程退出监控
			String existValue = centerDataService.get(monitorKey);
			if(existValue != null && !existValue.equals(identify)){
				logger.info("用户消息队列:" + monitorKey + "]已经由线程:" + existValue + "监控，本监控线程:" + identify + "退出");
				return;
			}
			centerDataService.setForce(monitorKey, identify, MONITOR_MAX_IDEL);
			Thread.sleep(WsConstants.USER_MQ_READ_INTERVAL);




			String key = WsConstants.USER_MQ_INSTANCE_PREFIX + "#" + wsSession.getUser().getUuid();
			String value = centerDataService.pushFromZQueue(key, false);
			if(value != null){
			//	for(int i = 0; i < SEND_MESSAGE_RETRY; i++){
						logger.debug("从消息队列中获取到了最早的消息，向session[" + wsSession + "]发送消息,消息长度:" + value.length());
						synchronized(s){
							s.channel().writeAndFlush(value);
						}
						logger.debug("从消息队列中获取到了最早的消息，向session[" + wsSession + "]发送消息:" + value + "，发送完成");

					//	break;
					
			//	}

			}

		}
	}

	@Override
	public void clearUserMessageQueue(EsSession wsSession) {
		String key = WsConstants.USER_MQ_INSTANCE_PREFIX + "#" + wsSession.getUser().getUuid();
		centerDataService.delete(key);
	}


	@Override
	public int sendMessageForce(ModelMap map, long... uuids) {
		return this.sendMessage(map, uuids);
	}




	@Override
	public List<EsSession> listOnPage(CriteriaMap esSessionCriteria) {
		return null;
	}


	@Override
	public EsSession get(long receiverId) {
		return null;
	}


	@Override
	public int sendLocalMessageDirect(long uuid, Object value, boolean forceSend) {
		return 0;
	}

}
