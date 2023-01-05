package com.maicard.boss.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 只允许内部用户访问本class或method
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalUserOnly {

}
