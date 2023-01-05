package com.maicard.views;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.core.entity.EisMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.maicard.utils.JsonUtils;


/**
 * 升级到Jackson 2.6.3, 
 * 使用@JsonView来控制前端的过滤
 * NetSnake,2015-11-15
 * 
 * 针对前台的JSON视图
 * 跟后台JSON处理的主要区别是需要过滤很多敏感信息
 * 
 * 
 * @author NetSnake
 * @date 2013-3-1
 */
public class PartnerJsonView extends MappingJackson2JsonView{

	protected final Logger logger = LoggerFactory.getLogger(getClass());



	private final String[] filterProperties = new String[] {"publicKeyExponent","publicKeyModulus","needCaptcha", "rememberMe", "userRememberName", "ownerId","pageSuffix","siteCode","siteStaticize","documentPrefix","contentPrefix","theme","welcomeName", "commonFooter","title"};


	@Override
	protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setContentType("application/json; charset=UTF-8");
		response.setHeader("Cache-Control",	"no-store, max-age=0, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");

		Object value = null;
		//貌似是spring 5以后增加了这个顶层
		if(map.containsKey("modelMap")) {
			value = 	super.filterModel(filter(map.get("modelMap")));
		} else {
			value = 	super.filterModel(filter(map));
		} 

		//ObjectMapper objectMapper = JsonUtils.getInstance();
		boolean jsonp = false;
		if(request.getRequestURI().endsWith(".jsonp")){
			jsonp =true;
		}
		//tree视图用于兼容easyUI
		boolean isTree = false;

		if(request.getRequestURI().endsWith(".tree")){
			isTree =true;
		}

		//标记view为Partner.class的属性才显示输出
		//logger.debug("value=" + value	);
		String responseString = JsonUtils.toStringApi(value);
		if(isTree){
			//查找map中的tree对象
			if(map.get("tree") != null){
				//已经是JSON格式的字符串，不需要进行转换，直接输出
				if(map.get("tree").toString().startsWith("[") && map.get("tree").toString().endsWith("]")){
					response.getWriter().write(map.get("tree").toString());
					return;						
				}
				if(map.get("tree").toString().startsWith("{") && map.get("tree").toString().endsWith("}")){
					response.getWriter().write(map.get("tree").toString());
					return;						
				}

			} else {
				logger.debug("请求返回tree数据，但map中没有tree对象.");
			}
		} else 	if(jsonp){
			String jsonpString = "jsonpback(";
			jsonpString += responseString;
			jsonpString += ")";
			responseString = jsonpString;
		} 
		response.getWriter().write(responseString);
	}

	private Map<String, Object>filter(Object obj){
		Map<String, Object> map = null;
		if(obj instanceof Map) {
			map  = (Map<String,Object>)obj;
		} else {
			return Collections.emptyMap();
		}		for(String properties : filterProperties){
			map.remove(properties);
		}
		return map;
	}


}
