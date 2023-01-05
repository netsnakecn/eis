package com.maicard.core.service.impl;

import com.maicard.core.constants.DataName;
import com.maicard.core.entity.Config;
import com.maicard.core.service.ConfigService;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.lang.annotation.Annotation;

/**
 * 要确保ContextUtil正确运行，需要在Spring启动APP中指定关联的2个service优先注入，例如：
 *
 */
@Component
@Slf4j
public final class ContextUtils implements    ApplicationContextAware, ServletContextAware {
    
    private static ApplicationContext context;

    private static         ServletContext servletContext;
    private static ConfigService configService;


    private static EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer;

    public static String[] getBeanNamesForType(Class<?> clazz) {
        return context.getBeanNamesForType(clazz);
    }

    public static int getIntProperty(final String key) {
        return NumericUtils.parseInt(encryptPropertyPlaceholderConfigurer.getProperty(key));
    }
    public static String getProperty(final String key) {
        return encryptPropertyPlaceholderConfigurer.getProperty(key);
    }
    public static boolean getBoolProperty(String key) {
        if(encryptPropertyPlaceholderConfigurer != null){
            return StringTools.isPositive(encryptPropertyPlaceholderConfigurer.getProperty(key));
        }
        return false;
    }


    public static  boolean getBooleanConfig(String configName){
        return configService.getBooleanValue(configName,0);

    }

    public static int getIntConfig(String configName, long ownerId){
        return configService.getIntValue(configName,0);
    }

    public static float getFloatConfig(String configName, long ownerId){
        return configService.getFloatValue(configName,0);
    }

    public static String getStringConfig(String configName, long ownerId){
        return configService.getValue(configName,0);
    }

    public static void setContext(ApplicationContext c) {
        context = c;
        log.debug("Application context initialized");
        initService();
    }

    /*
         @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            context = event.getApplicationContext();
            log.debug("Application context initialized by listener");
            initService();

        }*/
    private static void initService(){
        if(configService == null || encryptPropertyPlaceholderConfigurer == null){
            if(context != null){
                configService = context.getBean(ConfigService.class);
                encryptPropertyPlaceholderConfigurer = context.getBean(EncryptPropertyPlaceholderConfigurer.class);
            }
        }
    }
    public static Annotation findAnnotationOnBean(String beanName, Class clazz){
        return context.findAnnotationOnBean(beanName, clazz);
    }

    public static String getDataDir(){
        final String UPLOAD = "/upload";
        boolean autoDetect = true;
        String baseDir = configService.getValue(DataName.baseDir.name(),0);

        if(StringUtils.isBlank(baseDir)){
            //自动检测
            baseDir =  servletContext.getRealPath("/").replaceAll("/$", "");
            String[] data = baseDir.split("/");
            int offset = data.length - 3;
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < offset; i++){
                sb.append(data[i]).append(File.separator);
            }
            baseDir = sb.toString();
        }

        if(StringUtils.isBlank(baseDir)){
            baseDir = System.getProperty("user.dir");
        }
        String dataDir = baseDir.replaceAll("/$", "") + UPLOAD;
        log.debug("当前基本目录是:" +  baseDir + ",数据存储目录是:" + dataDir);
        return dataDir;

    }


    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name)      {
        try {
            return (T) context.getBean(name);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static <T> T getBean(Class<T> clz)      {
        try {
            return (T) context.getBean(clz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean containsBean(String name)
    {
        return context.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException
    {
        return context.isSingleton(name);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException
    {
        return context.getType(name);
    }

    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException
    {
        return context.getAliases(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker)
    {
        return (T) AopContext.currentProxy();
    }


    public static TaskExecutor getTaskExecutor(){
        return context.getBean(TaskExecutor.class);
    }


    @Override
    public void setServletContext(ServletContext c) {
        log.debug("Servlet context initialized");
        servletContext = c;
    }

    @Override
    public void setApplicationContext(ApplicationContext a) throws BeansException {
        context = a;
        log.debug("Application context initialized");
        initService();
    }
}