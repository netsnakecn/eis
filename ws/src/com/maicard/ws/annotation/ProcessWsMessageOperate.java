package com.maicard.ws.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * 在监听WS消息时，哪个实现类处理哪个或哪些路由码
 * @see OperateCode
 * 使用此注解的类，必须实现EisWsMessageListener接口
 * 
 * 
 * @author NetSnake
 * @date 2016-11-10
 */
@Retention(RetentionPolicy.RUNTIME)  
public @interface ProcessWsMessageOperate {
	String[] value();
	
	boolean needLogin = false;
}
