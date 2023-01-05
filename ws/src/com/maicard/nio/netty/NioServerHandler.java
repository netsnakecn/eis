package com.maicard.nio.netty;


import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.maicard.core.constants.EisError;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.utils.HttpUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.ws.annotation.ProcessWsMessageOperate;
import com.maicard.ws.entity.EsSession;
import com.maicard.ws.iface.WsMessageListener;
import com.maicard.ws.service.EsSessionService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;


@Service
@Scope("prototype")
@Sharable
public class NioServerHandler extends SimpleChannelInboundHandler<String> {


	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static ConcurrentHashMap<String, Set<WsMessageListener>> handlerMap = null;
	
	


	@Autowired
	private ApplicationContextService applicationContextService;

	@Autowired
	private EsSessionService wsSessionService;


	/*
	@Autowired
	private WsSessionService wsSessionService;*/

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
		/*Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");
		}
		channels.add(ctx.channel());*/
		//把新连接加入
		ChannelContainer.channels.add(ctx.channel());
		String sessionId = ctx.channel().id().asShortText();
		logger.info("来自[" + ctx.channel().remoteAddress() + "=>" + ctx.channel().localAddress() + "]的客户端[" + sessionId + "]建立连接,当前共有" + 	ChannelContainer.channels.size() + "个连接");
	} 

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
		/*Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n");
		}*/
		String sessionId = ctx.channel().id().asLongText();
		logger.info("来自[" + ctx.channel().remoteAddress() + "=>" + ctx.channel().localAddress() + "]的客户端[" + sessionId + "]被移除,当前共有" + 	ChannelContainer.channels.size() + "个连接");
		ChannelContainer.channels.remove(ctx.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {

		String sessionId = ctx.channel().id().asShortText();

		if(logger.isDebugEnabled())logger.debug("来自[" + ctx.channel().remoteAddress() + "=>" + ctx.channel().localAddress() + "]的客户端[" + sessionId + "]输入:" + message);

		EsSession wsSession = wsSessionService.get(sessionId);
		if(wsSession == null){
			wsSession = new EsSession(UUID.randomUUID().toString(),sessionId,0);
		}
		wsSessionService.put(wsSession,ctx);

		ModelMap map = new ModelMap();
		map.put("operate", Operate.notify.name());
		Map<String,String> params = HttpUtils.getRequestDataMap(message);
		String action = params.get("action");
		if(action == null){
			logger.error("消息中未提交操作代码，消息正文:" + message);
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id,"请提交操作代码"));
			ctx.channel().writeAndFlush(convert(map));
			return;
		}


		Date beginTime = new Date();

		if(handlerMap == null){
			initHandlerMap();
		}
		if(handlerMap == null || handlerMap.size()  < 1){
			logger.error("系统中没有类型为[" + WsMessageListener.class.getName() + "]的bean");
			map.put("message",EisMessage.error(EisError.beanNotFound.id,"动作处理列表为空"));
			ctx.channel().writeAndFlush(convert(map));
			return;
		}
		if(handlerMap.get(action) == null){
			logger.warn("当前系统中没有注册任何处理action=" + action + "的bean");
			map.put("message",EisMessage.error(EisError.unknownOperate.id,"不支持的操作"));
			ctx.channel().writeAndFlush(convert(map));		
			return;
		}
		if(logger.isDebugEnabled()){
			if(logger.isDebugEnabled())logger.debug("系统注册了" + handlerMap.get(action).size() + "个服务来处理action=" + action);
		}
		for(WsMessageListener bean : handlerMap.get(action)){
			if(logger.isDebugEnabled())logger.debug("把消息交给类[" + bean.getClass().getName() + "]处理操作:" + action);
			try {
				bean.onWsMessage(wsSession, params, map);
			} catch (Exception e) {
				logger.error("服务[" + bean.getClass().getName() + "]无法处理ES消息，服务抛出异常:" + e.getMessage());
				map.put("message", EisMessage.error(EisError.systemException.id,"系统异常"));
				e.printStackTrace();
			}
		}

		if(map.isEmpty()){
			if(logger.isDebugEnabled())logger.debug("ES消息[" + message + "]处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,但返回数据MAP为空，不发送数据");
		} else if(!ctx.channel().isOpen()){
			if(logger.isDebugEnabled())logger.debug("ES消息[" + message + "]处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,但Session已关闭");
		} else {
			String output = null;
			if(map.get("raw") != null){
				//需要发送原始数据
				output = map.get("raw").toString();
			} else {
				output = JsonUtils.toStringApi(map);
			}
			StringBuffer  brief = new StringBuffer();
			if(output.length() < 40){
				brief.append(output);
			} else {
				brief.append(output.substring(0, 38));
			}
			if(output.length() > 80){
				brief.append("...").append(output.substring(output.length() - 40));
			}
			if(logger.isDebugEnabled())logger.debug("ES消息[" + message + "]处理完成，耗时:" + (new Date().getTime() - beginTime.getTime()) + "毫秒,向Session发送消息:" + brief.toString());
			ctx.channel().writeAndFlush(output);
			//wsSessionService.sendMessage(map,session.getId());
		} 
		return;
		/*Attribute<User> attr = ctx.channel().attr(AttributeKey.valueOf("user"));
		if(msg.startsWith("LOGIN")){
			ctx.channel().writeAndFlush("SERVER:" + msg);
		}
		if(msg.startsWith("LOAD")){
			String[] param = msg.split("&");
			String className = param[1];
			byte[] classBin = this.loadClass(className);
			if(classBin == null || classBin.length < 1){
				logger.error("无法读取类:" + className);
				ctx.channel().writeAndFlush("SERVER:NONE");
			} else {
				String classHex = toHexString(classBin);
				logger.info("发送类:" + className + ",HEX长度:" + classHex.length());
				ctx.channel().writeAndFlush(classHex);

			}

		}*/
		/*
		WsSession wsSession = wsSessionService.get(ctx.channel().id().asLongText());
		if(wsSession == null){

		}*/

		//wsSessionService.put(wsSession,session);
	}

	private String convert(Object map) {
		return JsonUtils.toStringApi(map) + "\n";
	}

	/*
	 * 
	 * 覆盖 channelActive 方法 在channel被启用的时候触发 (在建立连接的时候)
	 * 
	 * channelActive 和 channelInActive 在后面的内容中讲述，这里先不做详细的描述
	 * */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		logger.info("客户端[" + ctx.channel().remoteAddress() + "]激活.");
		super.channelActive(ctx);
	}



	private  void initHandlerMap() throws InterruptedException {

		ApplicationContext applicationContext = null;

		while(applicationContext == null){
			logger.info("等待应用环境就绪...");
			Thread.sleep(2000);
			applicationContext = applicationContextService.getApplicationContext();
		}
		logger.info("已获取应用环境:" + applicationContext);

		Map<String,WsMessageListener>map  = null;
		try{
			map = applicationContext.getBeansOfType(WsMessageListener.class);
		}catch(Throwable t) {
			t.printStackTrace();
		}
		if(map == null || map.size() < 1){
			logger.error("系统中没有类型为[" + WsMessageListener.class.getName() + "]的bean");
			return;
		}
		logger.debug("从系统中获取到{}类型的bean{}个",WsMessageListener.class.getName(), map.size());
		
		for(String beanName : map.keySet()){

			ProcessWsMessageOperate processObjectAnnotation = applicationContext.findAnnotationOnBean(beanName, ProcessWsMessageOperate.class);

			if(processObjectAnnotation == null){			
				if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug(beanName + "未声明ProcessWsMessageOperate注解");
				}
				continue;
			}
			if(processObjectAnnotation.value() == null || processObjectAnnotation.value().length < 1){
				if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug(beanName + "的ProcessWsMessageOperate注解内容为空");
				}
				continue;
			}
			if(handlerMap == null){
				handlerMap = new ConcurrentHashMap<String, Set<WsMessageListener>>();
			}

			for(String value : processObjectAnnotation.value()){
				if(StringUtils.isBlank(value)){
					if(logger.isDebugEnabled())logger.debug("忽略[" + beanName + "]类的注解ProcessWsMessageOperate中的空指令");
					continue;
				}
				if(handlerMap.get(value) == null){
					handlerMap.put(value, new HashSet<WsMessageListener>());
				}
				handlerMap.get(value).add(map.get(beanName));
				/*if(logger.isDebugEnabled()){
					if(logger.isDebugEnabled())logger.debug("把bean" + beanName + "作为操作[" + value + "]的处理者");
				}*/
			}

		}

		logger.info("系统NIO处理器初始化完成，共注册:" + handlerMap.size() + "个指令及其处理器");


	}

}
