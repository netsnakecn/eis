package com.maicard.mb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 指定该对象是否需要从所有节点进行复制，即便这些数据不是后端节点或负责前端数据同步的节点
 *
 * @author NetSnake
 * @date 2015年8月15日 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedJmsDataSyncP2P {

}
