package com.maicard.mb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.*;

/**
 * 不要自动JMS数据同步
 * 由自身或其他服务自行实现JMS数据同步
 *
 * @author NetSnake
 * @date 2013-9-16 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreJmsDataSync {

}
