package com.maicard.mb.service.rabbitmq;



import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.core.service.ConfigService;


@Service
public class EisMessagePostProcessor extends BaseService implements MessagePostProcessor{
	
	@Resource
	private ConfigService configService;
	
	private String appId;
	
	@PostConstruct
	public void init(){
		appId = configService.getSystemCode() + "." + configService.getServerId();
	}

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		message.getMessageProperties().setAppId(appId);
		if(message.getMessageProperties().getCorrelationId() == null){
			message.getMessageProperties().setCorrelationId( UUID.randomUUID().toString());
		}
		message.getMessageProperties().setMessageId(message.getMessageProperties().getAppId() + "-" + UUID.randomUUID().toString());
		if(logger.isDebugEnabled()){
			//logger.debug("消息处理结束[messageId=" + message.getMessageProperties().getMessageId() + ",replyTo=" + message.getMessageProperties().getReplyTo() + ",correlationId=" + new String(message.getMessageProperties().getCorrelationId()) + "]");
		}
		return message;
	}

}
