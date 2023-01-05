package com.maicard.ws.iface;

import java.util.Map;

import org.springframework.ui.ModelMap;

import com.maicard.ws.entity.EsSession;

/**
 * 可以处理Socket Session消息的接口
 * 
 * 
 * @author NetSnake
 * @date 2013-3-3
 */
public interface WsMessageListener {
	
	void onWsMessage(EsSession esSession, Map<String, String> params, ModelMap map) throws Exception;

}
