package com.maicard.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 不要进行数据源切换
 * @author GHOST
 * @date 2020-03-25
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDs {

}
