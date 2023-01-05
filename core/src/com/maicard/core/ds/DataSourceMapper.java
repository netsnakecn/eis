package com.maicard.core.ds;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:56
 * 类型名称：DataSourceMapper
 * 类型意图：给service层提供注解映射数据源，value对应是spring中targetDataSources配置的key
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE,ElementType.METHOD})
public @interface DataSourceMapper {
    String value();
}
