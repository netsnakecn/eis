package com.maicard.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 不检查用户是否登录<br/>
 * 也就是非登陆状态也可以访问
 *
 *
 * @author NetSnake
 * @date 2016年5月11日
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreLoginCheck {

}
