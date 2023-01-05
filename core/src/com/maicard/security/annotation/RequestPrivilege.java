package com.maicard.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行该操作所需的权限配置
 * 
 * 
 * @author Iron
 * @date 2019-01-18
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestPrivilege {

    /**
     * 权限匹配对象代码
     * @return
     */
    String object() default "";

    /**
     * 操作代码
     * @return
     */
    String operate() default "";

}
