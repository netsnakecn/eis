/**
 * 
 */
package com.maicard.mb.service;

import org.springframework.scheduling.annotation.Async;

import com.maicard.core.entity.EisMessage;

/**
 * 
 *
 * @author NetSnake
 * @date 2013-9-16 
 */
public interface JmsDataSyncService {
	
	@Async
	void operate(EisMessage eisMessage);

}
