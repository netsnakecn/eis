package com.maicard.mb.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.HandlerEnum;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.annotation.ProcessMessageObject;
import com.maicard.mb.annotation.ProcessMessageOperate;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.service.JmsDataSyncService;
import com.maicard.utils.JsonUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 在本地执行由其他节点发送过来的数据同步请求
 * 每台物理服务器正常只应当由1个节点即1个tomcat来处理数据同步请求
 * 但是缓存删除的请求，每个节点都应当执行。
 *
 * @author NetSnake
 * @date 2013-9-16 
 */

@ProcessMessageOperate("JmsDataSync")
@ProcessMessageObject
public class JmsDataSyncServiceImpl extends BaseService implements JmsDataSyncService,EisMessageListener {

	@Resource
	private ApplicationContextService applicationContextService;

	//@Resource
//	private CacheService cacheService;

	@Resource
	private ConfigService configService;

	private boolean handlerJmsDataSyncToLocal = false;

	/*final ArrayList<String> execServiceOnBothServer = new ArrayList<String>(){
		private static final long serialVersionUID = 1L;

	{add("serverService");}};*/

	@PostConstruct
	public void init(){
		String v = configService.getProperty(HandlerEnum.HANDLE_SYNC.name());
		if(v == null || !v.equalsIgnoreCase("1")){

		} else {
			handlerJmsDataSyncToLocal = true;
		}
	}

	@Override
	@Async
	public void operate(EisMessage eisMessage) {
		String beanName = null;
		String methodName = null;
		Object parameters = null;
		try{
			beanName = eisMessage.getExtra("updateSlaveBeanName").toString();
			methodName = eisMessage.getExtra("updateSlaveMethodName").toString();
			parameters = eisMessage.getExtra("updateSlaveParamaters");

		}catch(Exception e){}
		if(beanName == null){
			logger.error("消息操作模式是更新副本，但未指定更新的bean名称");
			eisMessage = null;
			return;
		}
		if(methodName == null){
			logger.error("消息操作模式是更新副本，但未指定更新的bean方法");
			eisMessage = null;
			return;
		}
		Object bean = applicationContextService.getBean(beanName);
		if(bean == null){
			logger.warn("本节点中找不到指定的bean[" + beanName + "]");
			eisMessage = null;
			return;
		}
		

		//XXX 使用cglib代理，直接获取方法，无法获取到方法的注解，也无法使用Spring的LocalVariableTableParameterNameDiscoverer获取参数名
		//因此这里需要获取原始class中的方法
		Method[] methods = ClassUtils.getUserClass(bean).getMethods();
		if(methods == null || methods.length < 1){
			logger.warn("bean[" + beanName + "]没有可执行方法");
			eisMessage = null;
			return;
		}
		//logger.debug("对象[" + bean + "]共有:" + methods.length + "个方法");
		Object[] paraArray = null;
		try{
			paraArray = (Object[])parameters;	
		}catch(Exception e){
			e.printStackTrace();
		}
		if(paraArray == null){
			logger.error("无法将参数转换为Object[],参数类型是:" +  parameters.getClass().getName());
			return;
		}
		if(logger.isDebugEnabled()){
			logger.debug("接收到JMS数据同步请求:bean[" + beanName + "]类型:" + bean.getClass().getName() + ",方法是:" + methodName + "(" + parameters + "),请求参数共有:" + paraArray.length + "个");
		}
		boolean executed = false;
		Method execMethod = null;
		for(Method method : methods){
			//logger.debug("方法:" + methodName + "，与请求方法:" + methodName + ",方法的参数个数是:" + method.getParameterCount() + ",请求参数个数是:" + paraArray.length);
			if(method.getName().equals(methodName) && method.getParameterCount() == paraArray.length){	//XXX getParameterCount必须是JDK1.8以上
				/*boolean samePara = true;
				if(method.getParameterCount() > 0){
					for(int i = 0; i < method.getParameterCount(); i++){
						//XXX 只比较了类的simpleName，如果simpleName一样但是包名不一样的类，可能会出错，但是这种情况应该几率很小
						String type1 = method.getParameterTypes()[i].getSimpleName();
						String type2 = paraArray[i].getClass().getSimpleName();
						*//*if(method.getParameterTypes()[i].isPrimitive()){
							type2 = type2.replaceAll("java.lang.", "");
						}
						if(paraArray[i].getClass().isPrimitive()){
							type1 = type1.replaceAll("java.lang.", "");

						}*//*
						if(!type1.equalsIgnoreCase(type2)){
							logger.warn("方法的第{}个参数类型:{}与数据的类型:{}不一致", (i+1), method.getParameterTypes()[i].getName(), paraArray[i].getClass().getName());
							samePara = false;
						}
					}
				}
				if(!samePara){
					continue;
				}*/
				execMethod = method;
				break;
			}
		}
		if(execMethod == null){
			//找不到同名的方法
			logger.error("找不到bean[" + beanName + "]中的方法[" + methodName + "]，或其支持的参数:" + paraArray.length  + "不匹配");
			return;
		}
		logger.debug("找到了对象[" + bean + "]的同名方法:" + execMethod.getName());


		Class<?>[] types = execMethod.getParameterTypes();
		if(types == null || types.length < 1){
			logger.error("bean[" + beanName + "]中的方法[" + methodName + "]支持的参数为空,无法执行");
		}
		//如果系统使用了redis cache，则不需要检查cache和从本地删除缓存的操作
		boolean centerCache = isCenterCache();
		
		if(centerCache) {
		} else {
			CacheEvict cacheEvict = AnnotationUtils.findAnnotation(execMethod,CacheEvict.class);
			if(cacheEvict != null){
				String[] cacheNames = cacheEvict.value();
				if(cacheNames == null || cacheNames.length < 1){
					logger.error("错误的注解值，没有指定缓存名称cacheName");
				}
				String cacheKey = cacheEvict.key();
				String key = parseKey(cacheKey, execMethod, paraArray);
				logger.debug("服务[" +beanName + "]方法[" + methodName + "]具有CacheEvict注解,注解值:" + cacheKey + ",解析后的KEY=" + key);
				if(key == null){
					logger.error("无法解析注解值:" + parameters);
				} else {
					for(String cacheName : cacheNames){
						logger.debug("删除本地缓存[" + cacheName + "中的" + key + "]数据");
						CriteriaMap cacheCriteria = new CriteriaMap();
						cacheCriteria.put("cacheName",cacheName);
						cacheCriteria.put("key", key);
						//cacheService.delete(cacheCriteria);
					}
				}
			} 
		}
		//ExecOnBothNode execOnBothNode = AnnotationUtils.findAnnotation(execMethod,ExecOnBothNode.class);
		
		boolean runBothServer = false;//execOnBothNode != null ? true : false;
		if(handlerJmsDataSyncToLocal || runBothServer){
			if(logger.isDebugEnabled())logger.debug( (handlerJmsDataSyncToLocal ? "本节点负责将JMS的数据更新到本地" : "") + (runBothServer ? "该方法为所有节点均需执行" : "") + ",调用[" + beanName + "]执行方法[" + methodName + "],参数[" + JsonUtils.toStringFull(parameters) + "]");
			Object result = null;
			try {
				result = execMethod.invoke(bean, paraArray);
				executed = true;
			} catch (Exception e) {
				logger.error("在调用服务[" +beanName + "]执行方法[" + methodName + "],参数[" + parameters + "]时出错:",e);
			}

			if(executed){
				logger.debug("服务[" + beanName + "],方法[" + methodName + "]已完成调用:" + result);
			} else {
				logger.error("服务[" + beanName + "],方法[" + methodName + "]未能完成调用");

			}
		} else {

			if(logger.isDebugEnabled()){
				logger.debug("本节点不负责将JMS的数据更新到本地，忽略消息");
			}
		}

	}

	/**
	 * 检查是否为中央缓存模式
	 * 
	 * 
	 */
	private boolean isCenterCache() {
		return true;
		/*String cacheName = CacheNames.cacheNameSupport;
		Object nativeCache = cacheService.getNativeCache(cacheName);
		if(nativeCache == null) {
			logger.warn("找不到指定的缓存:{}", cacheName);
			return false;
		}
		logger.debug("原生缓存是:{}",nativeCache.getClass().getName());
		//有redis字样就是中央缓存
		return nativeCache.getClass().getName().toLowerCase().indexOf("redis") >= 0;*/
	}

	private String parseKey(String annotationValue, Method method, Object[] args){
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();  
		String[] paraNameArr = u.getParameterNames(method);
		if(paraNameArr == null || paraNameArr.length < 1){
			logger.warn("方法[" + method.getName() + "]参数为空，可能该方法是接口，或未使用cglib代理，因此无法获取方法的参数名列表");
			return null;
		} 
		ExpressionParser parser = new SpelExpressionParser(); 
		StandardEvaluationContext context = new StandardEvaluationContext();

		for(int i=0;i<paraNameArr.length;i++){
			context.setVariable(paraNameArr[i], args[i]);
		}
		String parsedValue = parser.parseExpression(annotationValue).getValue(context,String.class);
		return parsedValue;

	}

	@Override
	public void onMessage(EisMessage eisMessage) {
		operate(eisMessage);
		
	}
	}
