package com.maicard.views;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class FrontJsonView extends MappingJackson2JsonView{

	protected final Logger logger = LoggerFactory.getLogger(getClass());


	//private final String DEFAULT_JSON_INCLUSION = "NON_NULL";

	//private String jsonInclusion = null;




	private final String[] filterProperties = new String[] {"publicKeyExponent","publicKeyModulus","needCaptcha", "ownerId","pageSuffix","siteCode","siteStaticize","documentPrefix","contentPrefix","theme","welcomeName", "commonFooter","title"};


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
		/*ObjectMapper om = JsonUtils.getInstance();
		if(jsonInclusion == null){
			jsonInclusion = DEFAULT_JSON_INCLUSION;
		}
		if(jsonInclusion.equals("NON_DEFAULT")){
			om.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
		} else {
			om.setSerializationInclusion(JsonInclude.Include.NON_NULL);			
		}*/

		boolean jsonp = false;

		if(request.getRequestURI().endsWith(".jsonp")){
			jsonp =true;
		}


		String responseString = JsonUtils.toStringApi(value);

		if(jsonp){
			String jsonpString = "jsonpback(";
			jsonpString += responseString;
			jsonpString += ")";
			responseString = jsonpString;
		} 
		if(logger.isDebugEnabled()){
			if(responseString != null && responseString.length() > 200){
				logger.debug("向客户端输出:" + responseString.substring(0,199) + "...");
			} else {
				logger.debug("向客户端输出:" + responseString);

			}

		}
		response.getWriter().write(responseString);
	}

	private Map<String, Object>filter(Object obj){
		Map<String, Object> map = null;
		if(obj instanceof Map) {
			map  = (Map<String,Object>)obj;
		} else {
			return Collections.emptyMap();
		}
		//Map<String, Object> map2 = new HashMap<String, Object>();
		for(String properties : filterProperties){
			map.remove(properties);
		}
		return map;
	}

/*
	public void setJsonInclusion(String jsonInclusion){
		this.jsonInclusion = jsonInclusion;
	}
*/

}
