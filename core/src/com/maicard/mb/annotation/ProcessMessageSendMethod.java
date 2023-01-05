package com.maicard.mb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * 在监听用户消息时，处理哪些发送方式的消息，对象必须包含在Constants的UserMessageMethod枚举类型中
 * 
 * 
 * @author NetSnake
 * @date 2013-3-4
 */
@Retention(RetentionPolicy.RUNTIME)  
public @interface ProcessMessageSendMethod {
	String[] value();
}
