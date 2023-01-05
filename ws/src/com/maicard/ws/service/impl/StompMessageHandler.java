package com.maicard.ws.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import com.maicard.base.BaseController;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.utils.HttpUtils;
import com.maicard.ws.annotation.ProcessWsMessageOperate;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.iface.WsMessageListener;
import com.maicard.ws.service.EsSessionService;

/**
 * 用来接收STOMP用户订阅以及通过订阅通道向用户返回数据
 *
 *
 * @author NetSnake
 * @date 2016年11月13日
 *
 */
@Controller
public class StompMessageHandler extends BaseController{

	@Autowired(required=false)
	private SimpMessagingTemplate template;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private ApplicationContextService applicationContextService;
	
	@Autowired
	private EsSessionService wsSessionService;
	
	private static Map<String, WsMessageListener> handlerMap = null;


	/*@Autowired()
	public StompMessageHandler(SimpMessagingTemplate template) {
		this.template = template;
	}*/

/*	@SubscribeMapping("/init")
	public String init(){
		logger.debug("用户订阅消息初始化");
		return "init";
	}*/


	//广播形式的消息收发，发送到这里的消息，会发送到这个URL，然后所有订阅这个URL的用户都能收到
	@SuppressWarnings("rawtypes")
	@MessageMapping("/poker")
	@SendTo("/topic/eis")
	public EisMessage greeting(String text, Message message, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
		logger.debug("收到消息:" + text + ",payLoad=" + message.getPayload() + "，共有:" + attrs.size() + "个属性");
		for(String key : attrs.keySet()){
			logger.debug("当前属性:" + key + "=>" + attrs.get(key));
		}
		logger.debug("tempate:" + template.getClass().getName());
		return EisMessage.success("发送成功");
	}

	//用户自己的消息收发
	@MessageMapping("/message")
	@SendToUser
	public Map<String,Object> userMessage(String text, SimpMessageHeaderAccessor headerAccessor) throws Exception {
		
		ModelMap map = new ModelMap();

        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
		for(String key : attrs.keySet()){
			logger.debug("当前session属性:" + key + "=>" + attrs.get(key));
		}
		logger.debug("当前线程数:" + applicationContextService.getThreadCount());

		Map<String,String> params = HttpUtils.getRequestDataMap(text);
		String action = params.get("action");
		if(action == null){
			logger.error("消息中未提交操作代码，消息正文:" + text);
			map.put("message",EisMessage.error(EisError.REQUIRED_PARAMETER.id,"请提交操作代码"));
			return map;
		}
		Date beginTime = new Date();

		ApplicationContext applicationContext = applicationContextService.getApplicationContext();
		if(handlerMap == null){
			handlerMap  = applicationContext.getBeansOfType(WsMessageListener.class);
		}
		if(handlerMap == null || handlerMap.size()  < 1){
			logger.error("系统中没有类型为[" + WsMessageListener.class.getName() + "]的bean");
			return map;
		}
		if(logger.isDebugEnabled()){
			logger.debug("当前系统中有[" + handlerMap.size() + "]个注册为" + WsMessageListener.class.getSimpleName() + "]的bean");
			//logger.debug("当前活动用户个数:" + wsSessionService.count());
		}
		//FIXME esSession与native session已经不同了，所以这里不能通过native session的id得到wsSession
		EsSession wsSession = wsSessionService.get(headerAccessor.getSessionId());
		if(wsSession == null){
			Object o = attrs.get("ownerId");
			logger.debug("从附加属性中获取ownerId=" + o);
			long ownerId = 0;
			if(o != null && o instanceof Long){
				ownerId = (long)o;
			}
			wsSession = new EsSession(UUID.randomUUID().toString(), headerAccessor.getSessionId(),ownerId);
			wsSession.setServerId(configService.getServerId());
			wsSessionService.put(wsSession);
			logger.debug("向系统MAP中放入新的WsSession对象:" + headerAccessor.getSessionId() + ",ownerId=" + ownerId );
		}
		
		

		//Date beforeLoopTime = new Date();
		for(String beanName : handlerMap.keySet()){
			if(logger.isDebugEnabled()){
				logger.debug("尝试匹配服务[" + beanName + "]");
			}
			ProcessWsMessageOperate processObjectAnnotation = applicationContext.findAnnotationOnBean(beanName, ProcessWsMessageOperate.class);

			
			if(processObjectAnnotation == null){			
				if(logger.isDebugEnabled()){
					logger.debug(beanName + "未声明ProcessWsMessageOperate注解");
				}
				continue;
			}
			if(processObjectAnnotation.value() == null || processObjectAnnotation.value().length < 1){
				if(logger.isDebugEnabled()){
					logger.debug(beanName + "的ProcessWsMessageOperate注解内容为空");
				}
				continue;
			}
			
			for(String value : processObjectAnnotation.value()){
				if(logger.isDebugEnabled()){
					logger.debug("检查" + beanName + "声明ProcessWsMessageOperate注解值:" + value + ",与当前WS操作" + action);
				}
				if(value.equalsIgnoreCase(action)){
					logger.debug("类[" + beanName + "]定义了匹配的WS操作:" + action + ",消息交给它处理");
					handlerMap.get(beanName).onWsMessage(wsSession, params, map);
					break;
				}
			}

		}
		logger.debug("Stomp消息[" + text + "]处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒");
		return map;

/*		
		logger.debug("收到用户消息:" + userMessage);
		template.convertAndSend("/user/queue/message","世界消息");
		return userMessage;*/
	}


}
