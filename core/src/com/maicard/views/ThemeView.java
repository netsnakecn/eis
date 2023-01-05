package com.maicard.views;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.utils.JsonUtils;

public class ThemeView extends JstlView {
	protected final Logger logger = LoggerFactory.getLogger(getClass());



	private static Map<String,String> urlCache = new HashMap<String,String>();
	private static Map<String, List<String>> cachedIgnoreProperties = new ConcurrentHashMap<String, List<String>>();

	//private boolean filterView;

	@SuppressWarnings({ })
	@Override 
	protected void renderMergedOutputModel(Map<String,Object> origModel, HttpServletRequest request, HttpServletResponse response) throws Exception {  
		response.setContentType(getContentType());  
		String url = getUrl();
		String originalUrl = url;
		String siteCode = null;
		String bossTheme = null;
		Map<String,Object> model = null;
		if(origModel.containsKey("modelMap")) {
			model = 	(Map<String,Object>)origModel.get("modelMap");
		} else {
			model = origModel;
		}
		//logger.debug("传入ThemeView, map={}", JsonUtils.toStringFull(model.keySet()));

		logger.debug("原始URL是:" + originalUrl);
		if(urlCache == null){
			urlCache = new HashMap<String,String>();
		} else {
			logger.debug("URL缓存数量是:" + urlCache.size());
		}
		String cachedSiteCode = null;
		if(urlCache.containsKey(originalUrl)){
			logger.debug("从缓存中找到了对应的URL:" + originalUrl);
			cachedSiteCode = urlCache.get(originalUrl);
		}
		if(model.get("siteCode") != null){
			siteCode = model.get("siteCode").toString();
		} else if(model.get("bossTheme") != null){
			bossTheme = model.get("bossTheme").toString();
		}

		if(StringUtils.isNotBlank(siteCode)){
			if(cachedSiteCode != null && cachedSiteCode.equals(siteCode)){
				logger.debug("缓存中的siteCode与当前siteCode一致[" + siteCode + "],直接使用缓存中的URL:" + originalUrl);
				url = originalUrl;
			} else {
				if(cachedSiteCode != null){
					url = url.replaceAll(cachedSiteCode + "/","");
					logger.debug("删除之前的其他siteCode:" + cachedSiteCode + "删除后的URL:" + url);
					originalUrl = url;
				}
				String pattern = "/WEB-INF/jsp/(\\w+\\.\\w+)/\\S+";
				siteCode = siteCode.replaceAll("^/", "").replaceAll("/$", "");
				if(url.indexOf(siteCode) < 0){
					Pattern p = Pattern.compile(pattern);
					Matcher m = p.matcher(url);
					if(m.matches()){
						String replace = m.group(1);
						url = url.replaceAll(replace,siteCode);

						logger.debug("将原始资源[" + originalUrl + "]替换为:" + url);
					} else {
						url = url.replaceAll("/WEB-INF/jsp/", "/WEB-INF/jsp/" + siteCode + "/");
						logger.debug("使用站点[" + siteCode + "]来查找资源:" + url);
					} 
				} else {
					logger.debug("资源URL[" + url + "]已包含siteCode:" + siteCode);
				}
			}
		} else if(StringUtils.isNotBlank(bossTheme)){
			String pattern = ".*/(\\w+\\.jsp)$";
			bossTheme = bossTheme.replaceAll("^/", "").replaceAll("/$", "");
			if(url.indexOf(bossTheme) < 0){
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(url);
				if(m.matches()){
					String d = m.group(1);
					url = url.replaceAll(d, bossTheme + "/"+ d);
					logger.debug("将原始资源[" + originalUrl + "]替换为:" + url);
				} else {
					logger.warn("无法对URL[" + url + "]匹配bossTheme:" + bossTheme);
				} 
			} else {
				logger.debug("资源URL[" + url + "]已包含bossTheme:" + siteCode);
			}
		}
		String fileName = request.getSession().getServletContext().getRealPath(File.separator).replaceAll(File.separator + "$","") + url;
		File urlFile = new File(fileName);
		if(!urlFile.exists()){
			logger.debug("更改后的资源文件[" + fileName + "]不存在，使用原有URL:" + originalUrl + ",并放入缓存");
			super.setUrl(originalUrl);
			synchronized(this){
				urlCache.put(originalUrl, siteCode);
			}
		} else {
			super.setUrl(url);
			logger.debug("使用更改后的资源文件[" + fileName + "],并放入缓存[url=" + url + ",siteCode=" + siteCode + "]");
			synchronized(this){
				urlCache.put(url, siteCode);
			}
		}
		/*if(filterView){
			filter(model);
		}*/
		super.renderMergedOutputModel(model, request, response);
		//logger.debug("ThemeViewmap交由super处理后的map={}", JsonUtils.toStringFull(model.keySet()));


	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private void filter(Map model) {

	}
	/**
	 * 获得指定类及其所有祖先定义的属性，并检查是否定义了JsonView，如果定义了是否与对应的viewName一致，如果不一致则不进行序列化
	 * @param object
	 * @param viewName
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<String> getIgnoreProperties(Object object, Class<?> viewClass) {
		if(cachedIgnoreProperties != null && cachedIgnoreProperties.size() > 0){
			if(cachedIgnoreProperties.get(object.getClass().getName()) != null){
				if(logger.isDebugEnabled()){
					//logger.debug("从缓存中返回对象[" + object.getClass().getName() + "]的忽略属性列表");
				}
				return cachedIgnoreProperties.get(object.getClass().getName());
			}
		}
		if(logger.isDebugEnabled()){
			//logger.debug("分析对象[" + object.getClass().getName() + "]针对VIEW:" + viewClass.getName() + "]的属性过滤");
		}
		List<String> ignorePropertes = new ArrayList<String>();
		ignorePropertes.add("class");
		if(viewClass == null){
			if(logger.isDebugEnabled()){
				//logger.debug("当前序列化未指定viewClass");
			}
		} else {
			@SuppressWarnings("rawtypes")
			Class targetClass = object.getClass();
			for(; targetClass != Object.class ; targetClass = targetClass.getSuperclass()) {
				for(Field f : targetClass.getDeclaredFields()){
					if(logger.isDebugEnabled()){
						//logger.debug("检查类[" + targetClass.getName() + "]属性:" + f.getName());
					}
					Annotation annotation = f.getAnnotation(JsonView.class);
					if(annotation != null){
						JsonView view = (JsonView)annotation;
						Class<?>[] data = view.value();
						for(Class<?> clazz : data){
							if(logger.isDebugEnabled()){
								//logger.debug("属性[" + f.getName() + "]拥有的JsonView注解是[" + clazz.getName() + "]");
							}
							boolean addIgnore = true;
							//XXX 循环VIew的class及其父类，如果有与定义的JsonView父类同名的就认为不需要忽略
							for(; viewClass != Object.class ; viewClass = viewClass.getSuperclass()) {
								if(viewClass.getName().equals(clazz.getName())){
									if(logger.isDebugEnabled()){
										//logger.debug("属性[" + f.getName() + "]拥有的JsonView注解[" + clazz.getName() + "]与当前序列化指定的JsonView[" + viewClass.getName() + "]一致，应当输出");
									}
									addIgnore = false;
									break;
								}
							}
							if(addIgnore){
								ignorePropertes.add(f.getName());
							}
						}
					} else {
						//logger.debug("属性:" + f.getName() + "]没有@JsonView");
					}

				}
			}
		}

		cachedIgnoreProperties.put(object.getClass().getName(), ignorePropertes);
		return ignorePropertes;
	}
/*
	public void setFilterView(boolean filterView) {
		this.filterView = filterView;
	}*/
}
