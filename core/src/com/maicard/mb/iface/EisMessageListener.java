package com.maicard.mb.iface;

import org.springframework.scheduling.annotation.Async;

import com.maicard.core.entity.EisMessage;


/**
 * 可以处理系统消息的接口
 * 
 * 
 * @author NetSnake
 * @date 2013-3-3
 */
public interface EisMessageListener {
	
	@Async
	public void onMessage(EisMessage eisMessage);

}
