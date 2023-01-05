package com.maicard.security.annotation;

import static com.maicard.core.constants.Constants.DEFAULT_STRICT_AUTH_MODE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 指定一个方法是否需要进行严格验证,严格验证方法包括需要输入支付密码payPassword或者是当前密码userPassword
 * ttl指定严格验证后多长时间内可以不需要再次输入密码，如果是0，则每次都需要输入密码
 *
 *
 * @author NetSnake
 * @date 2017-09-26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StrictAuthorize {
	
	/**
	 * 该方法严格验证时对应的token
	 */
	String token();
	
	/**
	 * 验证类型
	 */
	String authType() default DEFAULT_STRICT_AUTH_MODE;	
	
	/**
	 * 验证有效期
	 * @return
	 */
	long ttl() default 0;


}
