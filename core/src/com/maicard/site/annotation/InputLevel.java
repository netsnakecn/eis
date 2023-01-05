package com.maicard.site.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.maicard.views.JsonFilterView.Front;
import com.maicard.views.JsonFilterView.Full;

/**
 * 该字段是否可以在管理后台作为一个编辑项输入
 *
 *
 * @author NetSnake
 * @date 2016年6月21日
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InputLevel {
	Class<? extends Front> value() default Full.class;

}
