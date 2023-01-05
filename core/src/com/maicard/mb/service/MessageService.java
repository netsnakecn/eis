package com.maicard.mb.service;

import java.io.Serializable;


public interface MessageService {

	String send(String destination, Serializable m);
	
	void sendJmsDataSyncMessage(String destination, String beanName, String methodName, Object... object);

	Object sendAndReceive(String destination, Serializable m);

	void reply(String destination, String queue, String correlationId, Serializable m);

}
