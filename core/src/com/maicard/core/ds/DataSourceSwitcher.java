package com.maicard.core.ds;

import com.maicard.base.DataBaseContextHolder;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:53
 * 线程变量存储当前线程DataSource的key值
 */
public class DataSourceSwitcher {
    public static void clear(){
        LazyHolder.sc.threadName.remove();
    }

    public static String getDataSourceType(){
        return LazyHolder.sc.threadName.get();
    }

    public static void setDataSourceType(String type){
        LazyHolder.sc.threadName.set(type);
        DataBaseContextHolder.setDbSource(type);

    }

    private static class LazyHolder{
        private static final DataSourceSwitcher sc=new DataSourceSwitcher();
    }

    private final ThreadLocal<String> threadName = new ThreadLocal<String>();
}
