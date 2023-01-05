package com.maicard.core.aspect;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.maicard.base.BaseService;
import com.maicard.base.ImplNameTranslate;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.annotation.IgnoreJmsDataSync;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.service.MessageService;

/*
 * 数据更新切面
 * 当服务更新数据后，发送更新SLAVE数据的消息
 */
//@Aspect
public class JmsDataSyncAspect extends BaseService{

	@Resource
	private ApplicationContextService applicationContextService;
	@Resource
	private ConfigService configService;
	@Resource
	private MessageService messageService;

	private String messageBusName;

	private boolean isSomeMasterHandler = false;

	@PostConstruct
	public void init(){
		messageBusName = configService.getValue(DataName.messageBusUser.toString(),0);
		if(messageBusName == null){
			logger.warn("系统未配置参数[messageBusName]");
		}
		/*isSomeMasterHandler = configService.getBooleanValue(DataName.handlerBossUpdate.toString(),0)
				|| configService.getBooleanValue(DataName.handlerGiftCardUpdate.toString(),0)
				||	configService.getBooleanValue(DataName.handlerGlobalUnique.toString(),0)
				||	configService.getBooleanValue(DataName.handlerItem.toString(),0)
				||	configService.getBooleanValue(DataName.handlerMoney.toString(),0)
				||	configService.getBooleanValue(DataName.handlerOperateLog.toString(),0)
				||	configService.getBooleanValue(DataName.handlerPay.toString(),0)
				||	configService.getBooleanValue(DataName.handlerProductUpdate.toString(),0)
				||	configService.getBooleanValue(DataName.handlerUserMessageUpdate.toString(),0)
				||	configService.getBooleanValue(DataName.handlerUserDataUpdate.toString(),0);		*/

	}


	/*@Around("(execution(* com..service..*.update*(..)) "
			+ "|| execution(* com..service..*.insert*(..)) " 
			+ "|| execution(* com..service..*.delete*(..)) " 
			+ "|| execution(* com..service..*.move*(..)) " 
			+ "|| execution(* com..service..*.sync*(..)) " 
			+ "|| execution(* com..service..*.load(..)) " 
			+ ") && (!@annotation(com.maicard.mb.annotation.IgnoreJmsDataSync))")*/
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
		
		
		boolean mqEnabled = configService.getBooleanValue(DataName.MQ_ENABLED.name(),0);
		if(!mqEnabled) {
			Object result = joinPoint.proceed();
			return result;
		}
		if(logger.isDebugEnabled()){
			logger.debug("切入数据更新, bean：" + joinPoint.getTarget().getClass().getSimpleName() + ", method:" +joinPoint.getSignature().getName() + ",参数个数:" +  joinPoint.getArgs().length);
		}

		String beanName = ImplNameTranslate.translate(joinPoint.getTarget().getClass().getSimpleName());// StringUtils.uncapitalize(joinPoint.getTarget().getClass().getSimpleName()).replace(CommonStandard.implBeanNameSuffix, "");
		String methodName = joinPoint.getSignature().getName();
		BaseEntity eisParameters = null;
		Object result = joinPoint.proceed();
		Object[] params = joinPoint.getArgs();

/*		if(joinPoint.getArgs().length != 1){
			logger.debug("数据更新只支持1个参数，忽略");
			return result;
		} 	
*/		Signature  signature = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;  
		Method method = methodSignature.getMethod();
		//FIXME 正常按照注解的书写，不应该拦截具有IgnoreJmsDataSync属性的方法，但是还是会出现拦截的情况，因此这里再次做个判断
		if(method.isAnnotationPresent(IgnoreJmsDataSync.class)){
			logger.debug("方法拥有IgnoreJmsDataSync注解，不进行同步");
			return result;
		}
		EisMessageListener eisMessageListener = null;
		try{
			eisMessageListener = (EisMessageListener)joinPoint.getTarget();
		}catch(ClassCastException e){}
		if(eisMessageListener != null){
			if(logger.isDebugEnabled()){
				logger.debug("目标服务是一个MessageListener实现，JMS动态更新由其自身完成，忽略");
			}
			return result;
		}
		if(params != null && params.length > 0 && params[0] != null){		
			logger.debug("第一个参数类型是[" + params[0].getClass().getName() + "]");
			if(params[0] instanceof BaseEntity){
				eisParameters = (BaseEntity)params[0];
			}
		} else {
			logger.debug("第一个参数为空");
		}
		
		/*boolean isDelete = false;
		if(eisParameters == null){
			if(methodName.startsWith("delete")){
				isDelete = true;
			}
		}
		if(eisParameters == null && !isDelete){
			logger.debug("参数类型既不是BaseEntity，执行方法也不是delete开头，忽略");
			return result;
		}*/
		boolean jmsDataSyncFromAll = false;
		if(eisParameters != null){
			if(method.isAnnotationPresent(NeedJmsDataSyncP2P.class)) {
				logger.debug("方法[" + beanName + "." + methodName + "]拥有NeedJmsDataSyncP2P注解，将执行JMS数据同步");
				jmsDataSyncFromAll = true;
				
			} else if(eisParameters.getClass().isAnnotationPresent(NeedJmsDataSyncP2P.class)) {
				logger.debug("类[" + eisParameters.getClass().getName() + "]拥有NeedJmsDataSyncP2P注解，将执行JMS数据同步");
				jmsDataSyncFromAll = true;
			}
		
		}
		if(!isSomeMasterHandler && !jmsDataSyncFromAll){
			if(logger.isDebugEnabled()){
				logger.debug("本节点不是任何一种数据处理节点，对象也没有NeedJmsDataSyncP2P注解，忽略");
			}
			return result;		
		}
		
		boolean execSucess = false;
		int rs = -2;

		if(result instanceof EisMessage){
			rs = ((EisMessage)result).getCode();
			if(rs == OpResult.success.id){
				execSucess = true;
			}
		} else {
			try{
				rs = (Integer)result;
			}catch(ClassCastException e1){
				logger.error("方法返回值不是int型，直接完成不进行后续处理");
				return result;

			}catch(Exception e){}
			if(rs > 0 && rs < 500000){
				execSucess = true;
			}
		}

		if(execSucess){
			logger.debug(beanName + "." + methodName + "被切入方法执行完成");

			EisMessage syncMessage = new EisMessage();
			syncMessage.setCode(Operate.JmsDataSync.id);

			syncMessage.setExtra("updateSlaveBeanName", beanName);	
			syncMessage.setExtra("updateSlaveMethodName", methodName);
			if(eisParameters != null){
				if(eisParameters.getSyncFlag() == 0){
					eisParameters.setSyncFlag(1);
					params[0] = eisParameters;
					logger.debug("修改后的Eis对象是:" + eisParameters.toString());
				} else {
					if(logger.isDebugEnabled()){
						logger.debug("被切入方法执行完成，但参数的syncFlag已被设置为1，不再发送副本更新消息");
					}
					return result;
				}
			} 		
			syncMessage.setExtra("updateSlaveParamaters", params);

			try{
				if(logger.isDebugEnabled()){
					logger.debug("发送副本更新消息，请求执行[" + beanName + "]的方法[" + methodName + "]");
				}
				messageService.send(messageBusName, syncMessage);
				syncMessage = null;
			}catch(Exception e){
				e.printStackTrace();
			}
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("被切入方法执行完成，但返回值是:" + rs + ",忽略更新");
			}
		}

		return result;
	}

	/*private String parseKey(String annotationValue, Method method, Object[] args){
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

	}*/
}
