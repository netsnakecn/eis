package com.maicard.ws.service.impl;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.maicard.base.BaseService;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.service.ConfigService;
import com.maicard.security.entity.User;
import com.maicard.security.service.CertifyService;
import com.maicard.site.service.SiteDomainRelationService;
 

@Service
public class EisWebSocketHandShakeInterceptor extends BaseService implements HandshakeInterceptor  {

	@Autowired
	private ConfigService configService;
	
	@Autowired
	private SiteDomainRelationService siteDomainRelationService;
	
	@Autowired
	private CertifyService certifyService;
	
	@Override
	public void afterHandshake(ServerHttpRequest arg0, ServerHttpResponse arg1, WebSocketHandler arg2, Exception arg3) {
		
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getURI().getHost());
		long ownerId = 0;
		if(siteDomainRelation == null){
			logger.error("无法得到当前请求的站点关联数据:" + request.getURI().toString());
			return false;
		} else {
			ownerId = siteDomainRelation.getOwnerId();
			attributes.put("ownerId", ownerId);
			logger.debug("根据当前请求[" + request.getURI().toString() + "]得到的站点关联数据:" + siteDomainRelation);
		}
		User frontUser = null;
		try {
		//检查是否有登录用户信息
			frontUser = certifyService.loginByToken(request);
		}catch(Exception e) {
			e.printStackTrace();
		}
		logger.info("从请求中获取的登录用户是:{}", frontUser);
		if(frontUser != null) {
			
			attributes.put("frontUser",frontUser);
		}
		
		attributes.put("serverId", configService.getServerId());
		attributes.put("clientIp", request.getRemoteAddress().getHostString());
		String esSessionId = UUID.randomUUID().toString();
		attributes.put("esSessionId",esSessionId);
		attributes.put("serverURL", request.getURI().toString());
		if(logger.isDebugEnabled()){
			logger.debug("来自[" + request.getRemoteAddress().getHostString() + "]的客户开始连接握手，放入新的esSessionId=" + esSessionId + ",ownerId=" + ownerId);
		}
       /* if (request instanceof ServletServerHttpRequest) { 
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;  
            logger.debug("当前请求信息:" + servletRequest.getURI().toString());
            if (session != null) {  
                String userName = (String) session.getAttribute(Constants.SESSION_USERNAME);  
                logger.info(userName+" login");  
                attributes.put(Constants.WEBSOCKET_USERNAME,userName);  
            }else{  
                logger.debug("httpsession is null");  
            }  
        }  */
        return true; 
       }

}
