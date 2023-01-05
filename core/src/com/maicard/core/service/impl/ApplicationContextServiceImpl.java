package com.maicard.core.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import com.maicard.base.BaseService;
import com.maicard.core.constants.DataName;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.site.service.SiteDomainRelationService;


@Service
public class ApplicationContextServiceImpl extends BaseService implements ApplicationContextService,ApplicationContextAware,ServletContextAware {




	private ApplicationContext applicationContext;

	private ServletContext servletContext;



	static {
		try {  
			Class.forName("java.time.Clock");
		} catch (Exception e) {  
			throw new UnsupportedClassVersionError("系统至少需要Java1.8才能正常运行");
		}  
	}




	@PostConstruct
	public void init(){
		logger.info("完成应用程序加载");
	}

	@Override
	public Object getBean(String beanName) {
		if(beanName == null || beanName.equals("")){
			return null;
		}
		try{
			return applicationContext.getBean(beanName);
		}catch(Exception e){}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T>T getBeanGeneric(String beanName) {
		if(StringUtils.isBlank(beanName)){
			return null;
		}
		try{
			return (T)applicationContext.getBean(beanName);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public <T>T getBeanGeneric(Class<T> clazz) {
		if(clazz == null){
			return null;
		}
		try{
			return (T)applicationContext.getBean(clazz);
		}catch(Exception e){}
		return null;
	}
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	/*
	 * 获取由本地message属性文件定义的描述
	 * 通常在spring配置文件中名为messageSource的bean来定义
	 */
	public String getLocaleMessage(String messageCode){
		return applicationContext.getMessage(messageCode, null, messageCode, null);
	}
/*
	@Override
	public String getTomcatStatus(){	
		MBeanServer mBeanServer = null;
		if (MBeanServerFactory.findMBeanServer(null).size() > 0) {  
			mBeanServer =(MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);  
		} else {  
			mBeanServer = MBeanServerFactory.createMBeanServer();  
		}  
		ObjectName queryHosts = null;
		try {
			queryHosts = new ObjectName("*:j2eeType=WebModule,*");
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		}
		Set<ObjectName> hostsON = mBeanServer.queryNames(queryHosts, null);
		Iterator<ObjectName> iterator = hostsON.iterator();
		while (iterator.hasNext()) {
			ObjectName contextON = iterator.next();
			String webModuleName = contextON.getKeyProperty("name");
			if (webModuleName.startsWith("//")) {
				webModuleName = webModuleName.substring(2);
			}
			System.out.println("XX=================" + webModuleName);

		}
		return null;
	}*/


	


	@Override
	public int getThreadCount(){
		return ManagementFactory.getThreadMXBean().getThreadCount();				
	}

	@Override
	public long getTotalStartedThreadCount(){
		return ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();				
	}


	@Override
	public float getPerformanceRate(){
		long beginTs = ManagementFactory.getRuntimeMXBean().getStartTime();
		long runningTs = (new Date().getTime() - beginTs) / 1000;
		List<GarbageCollectorMXBean> instances  = ManagementFactory.getGarbageCollectorMXBeans();
		long totalGcCount = 0;
		for(GarbageCollectorMXBean instance : instances){
			totalGcCount += instance.getCollectionCount();
		}
		float rate = (float)totalGcCount / (float)runningTs;
		logger.debug("系统已运行" + runningTs + "秒，GC" + totalGcCount + "次，性能指数:" + rate);
		return rate;

	}


	@Override
	public void directResponseException(HttpServletRequest request, HttpServletResponse response, Throwable t) throws Exception{
		SiteDomainRelationService siteDomainRelationService = getBeanGeneric("siteDomainRelationService");
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("无法获取到ownerId");
			return;
		}
		String message = getLocaleMessage("exception." + t.getClass().getSimpleName());
		if(message == null || message.equals("")){
			message = t.getMessage() == null ? t.getClass().getName():t.getMessage();
		}
		String resultTemplate= "";
		try{
			ConfigService configService = getBeanGeneric("configService");
			resultTemplate = configService.getValue("site.directResultPage",siteDomainRelation.getOwnerId());
		}catch(Exception e){}
		if(resultTemplate == null || resultTemplate.equals("")){
			resultTemplate = "/WEB-INF/jsp/message/directResult.jsp";
		}
		resultTemplate = this.servletContext.getRealPath(resultTemplate);
		//读取模版
		File file = new File(resultTemplate);
		StringBuffer templateContent = new StringBuffer();
		if(!file.exists()){
			logger.error("无法读取直接错误模板:" + resultTemplate);
		} else {
			BufferedReader bufread= new   BufferedReader   (new   InputStreamReader(new   FileInputStream(resultTemplate),"UTF-8"));
			String read = "";
			while((read=bufread.readLine()) != null)
			{
				templateContent.append(read);
			}	
			bufread.close();
		}
		String content = templateContent.toString();
		try{
			content = content.replaceAll("\\$\\{message\\}", message);
		}catch(Exception e){
			e.printStackTrace();
		}

		StringBuffer cause = new StringBuffer();
		StackTraceElement[] stackArray = t.getStackTrace();
		for (int i = 0; i < stackArray.length; i++) {
			StackTraceElement element = stackArray[i];
			cause.append(element.toString() + "\n"); 
		}
		String caouseString = cause.toString();
		caouseString = caouseString.replaceAll("\\$", "");
		//logger.info(caouseString);
		content = content.replaceAll("\\$\\{cause\\}", caouseString);


		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();

		writer.println(content);
		writer.flush();
		writer.close();	
		content = null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String[] getBeanNamesForType(Class clazz) {
		return applicationContext.getBeanNamesForType(clazz);
	}

	@Override
	public ApplicationContext getApplicationContext() {		
		logger.info("XXXX获取程序的环境");
		return applicationContext;
	}



	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Annotation findAnnotationOnBean(String beanName, Class clazz){
		return applicationContext.findAnnotationOnBean(beanName, clazz);
	}

	@Override
	public String getDataDir(){
		final String UPLOAD = "/upload";
		boolean autoDetect = true;
		ConfigService configService = getBeanGeneric("configService");
		String baseDir = configService.getValue(DataName.baseDir.name(),0);
		
		if(StringUtils.isBlank(baseDir)){
			if(this.servletContext == null){
				logger.error("Servlet context 未准备好");
				return null;
			}
			//自动检测
			autoDetect = true;
			baseDir =  this.servletContext.getRealPath("/").replaceAll("/$", "");
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
		logger.debug("当前基本目录是:" +  baseDir + ",自动检测:" + autoDetect + ",数据存储目录是:" + dataDir);
		return dataDir;

	}



	@Override
	public ServletContext getServletContext() {		
		return this.servletContext;
	}

	@Override
	public void setServletContext(ServletContext arg0) {
		this.servletContext = arg0;

	}


}
