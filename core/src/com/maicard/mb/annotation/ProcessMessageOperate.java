package com.maicard.mb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * 在监听系统消息时，处理哪些操作，对象必须包含在Constants的Operate枚举类型中
 * 使用此注解的类，必须实现EisMessageListener接口
 * 
 * 
 * @author NetSnake
 * @date 2013-3-4
 */
@Retention(RetentionPolicy.RUNTIME)  
public @interface ProcessMessageOperate {
	String[] value() default "*";
}
