package com.maicard.ws.service.impl;


import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;

/**
 * 暂未使用的客户端断开链接处理逻辑
 * 因为更具体的业务逻辑也需要处理客户断开的事件，防止出现冲突。
 *
 *
 * @author NetSnake
 * @date 2016年11月15日
 *
 */
@Service
public class StompClientDisconnectListner extends BaseService {
/*
	@Resource
	private OperateLogService operateLogService;

	@Resource
	private WsSessionService wsSessionService;


	@Override
	public void onApplicationEvent(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		logger.debug("STOMP客户端[" + sessionId + "]已断开连接,删除连接信息");
		if(sessionId == null){
			logger.error("断开连接没有sessionId");
			return;
		}
		WsSession wsSession = wsSessionService.get(sessionId);
		if(wsSession == null){
			logger.warn("在系统在线数据中找不到STOMP客户端[" + sessionId + "]的记录");
			return;
		}
		wsSessionService.remove(sessionId);

		User frontUser = wsSession.getUser();
		if(frontUser == null){
			logger.warn("在系统在线数据中找不到STOMP客户端[" + sessionId + "]的对应用户对象");
			return;
		}
		operateLogService.insert(new OperateLog(ObjectType.user.getCode(), frontUser.getUsername(), frontUser.getUuid(), OperateCode.USER_LOGOUT.toString(), String.valueOf(OperateResult.accept.getId()), null, null,0));



	}*/
}
