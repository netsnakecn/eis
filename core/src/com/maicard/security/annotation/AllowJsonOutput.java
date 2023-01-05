package com.maicard.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 一个方法必须使用此注解，才允许返回json数据
 *
 *
 * @author NetSnake
 * @date 2017-10-27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowJsonOutput {

}
