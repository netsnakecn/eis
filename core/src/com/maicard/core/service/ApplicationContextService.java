package com.maicard.core.service;

import java.lang.annotation.Annotation;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;


public interface ApplicationContextService {
	
	/**
	 * 根据名字返回一个Spring bean<br>
	 * 已由getBeanGeneric取代，直接返回泛型
	 * @param beanName
	 * @return
	 */
	Object getBean(String beanName);



	<T>T getBeanGeneric(Class<T> clazz);
	/**
	 * 泛型根据名字返回一个指定类型的Spring bean<br>
	 * @param beanName
	 * @return
	 */
	<T>T getBeanGeneric(String beanName);
	
	
	/**
	 * 获取系统配置的message中对应代码的说明
	 * @param messageCode
	 * @return
	 */
	String getLocaleMessage(String messageCode);
	
	/**
	 * 抛出异常而不是打印异常错误
	 * @param request
	 * @param response
	 * @param t
	 * @throws Exception
	 */
	void directResponseException(HttpServletRequest request,
			HttpServletResponse response, Throwable t)
			throws Exception;
	
	/**
	 * 获取当前JVM的总线程数
	 * @return
	 */
	int getThreadCount();
	
	/**
	 * 获取当前JVM自启动以来创建的总线程数
	 * @return
	 */
	long getTotalStartedThreadCount();
	
	
	
	/**
	 * 根据类型返回所有匹配的Spring bean名字列表
	 * @param clazz
	 * @return
	 */
	String[] getBeanNamesForType(@SuppressWarnings("rawtypes") Class clazz);
		
	
	
	/**
	 * 直接获取当前的ServletContext
	 * @return
	 */
	ServletContext getServletContext();
	
	
		
	/**
	 * 查找一个Spring bean中类型为clazz的注解
	 * @param beanName
	 * @param clazz
	 * @return
	 */
	Annotation findAnnotationOnBean(String beanName, Class<?> clazz);
	
	float getPerformanceRate();

	/**
	 * 直接获取当前的ApplicationContext(
	 * @return
	 */
	ApplicationContext getApplicationContext();
	
	/**
	 * 获取当前系统存放各种数据的基本目录<br>
	 * 如果系统未配置，则自动检测当前应用环境的父目录下的/upload目录
	 * @return
	 */
	String getDataDir();
	

	
}
