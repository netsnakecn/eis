package com.maicard.ws.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.maicard.base.BaseService;
import com.maicard.ws.constants.WsConstants;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.service.EsSessionService;

/**
 * 监视由本节点负责的所有活动连接，是否都有对应线程在监视用户消息队列
 *
 *
 * @author GHOST
 * @date 2017-12-18
 */

@Service
public class UserMessageQueueMonitor extends BaseService {
	
	@Resource
	private EsSessionService esSessionService;

	static boolean running = false;
	
	@Autowired
	private EisWebSocketHandler eisWebSocketHandler;
	@Scheduled(initialDelay=2000, fixedDelay=1000 * 180) 
	public void run(){
		if(running){
			logger.info("消息队列监控已启动，本次不启动");
			return;
		}
		running = true;
		this.process();
	}

	private void process(){
		while(true){

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			if(EsSessionContainer.localWsSessionMap.size() < 1){
				logger.debug("本地EsSession列表为空");
				continue;
			}
			List<EsSession> removeSession = new ArrayList<EsSession>();
			for(EsSession esSession : EsSessionContainer.localWsSessionMap.values()){
				if(esSession.getUser() == null){
					if(esSession.getCreateTime() == null){
						esSession.setCreateTime(new Date());
					}
					if(new Date().getTime() - esSession.getCreateTime().getTime() > WsConstants.ES_SESSION_NO_USER_IDLE_MS){
						logger.error("esSession#{}中的用户为空，并且超过了最长等待时间{}ms，断开该Session", esSession.getEsSessionId());
						
						removeSession.add(esSession);
					}
					
					continue;
				}
				
				long lastPingTime = esSession.getLongExtra("lastPingTime");
				long reduceTime = System.currentTimeMillis()-lastPingTime;
				logger.debug("esSession[{}]距离上次收到ping的时间已经过去{}ms",esSession.getEsSessionId(),reduceTime);
				boolean sendMessage = false;
				if (esSession.getExtra("isDisconnec") == null || esSession.getExtra("isDisconnec").equals("true")) {
					sendMessage = true;
				}
				/*if (lastPingTime > 0 && reduceTime/1000  > 20 && sendMessage) {
					logger.debug("esSession[{}]已经20秒没收到ping消息,认为客户端已断线",esSession.getEsSessionId());
					WebSocketSession webSocketSession = EsSessionContainer.localNativeSessionMap.get(esSession.getNativeSessionId());
					if (webSocketSession != null) {
						eisWebSocketHandler.handleTextMessage(webSocketSession, new TextMessage("action=DISCONNECT"));
						logger.debug("webSocketSession[{}]发送玩家断线消息",webSocketSession.getId());
					}
				}*/
				long uuid = esSession.getUser().getUuid();
				if(EsSessionContainer.userMessageHandlerMap.containsKey(uuid)){
					//在监控中注册了，检查时间
					long ts = EsSessionContainer.userMessageHandlerMap.get(uuid);
					long pass = new Date().getTime() - ts;
					//logger.debug("用户{}EsSession的监控程序上一次更新是:{},已过去{}ms",uuid, StringTools.getFormattedTime(ts), pass);
					if(pass > 2000){
						//logger.warn("用户#{}EsSession的监控程序上一次更新是:{},已过去{}ms，已超过2s", uuid,StringTools.getFormattedTime(ts), pass);
					}
				} else {
					//logger.warn("用户#{}的EsSession没有对应的监控程序", uuid);

				}

			}
			
			if(removeSession.size() > 0){
				for(EsSession esSession : removeSession){
					esSessionService.remove(esSession.getEsSessionId());
				}
				
			}
		}

	}
}
