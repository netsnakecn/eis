package com.maicard.mb.iface;

import com.maicard.mb.entity.UserMessage;

//发送、接收消息的网关服务
public interface MessageGateway  {
	boolean send(UserMessage message) throws Exception;
	boolean send(String receiver, UserMessage message);

}
