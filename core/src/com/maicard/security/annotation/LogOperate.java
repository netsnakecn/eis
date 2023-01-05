package com.maicard.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogOperate {

    /**
     * 权限匹配对象代码
     * @return
     */
    String objectTypeCode() default "";

    /**
     * 操作代码
     * @return
     */
    String operateCode() default "";
    /**
     *  y -- 年  M -- 月 d -- 天  H -- 小时 m -- 分
     * @return
     */
    int dateType() default Calendar.DATE;

    /**
     * 保留时长
     * @return
     */
    int lengthOfTime() default 1;

}
