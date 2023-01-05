package com.maicard.site.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentUtils {
	
	protected static final Logger logger = LoggerFactory.getLogger(AgentUtils.class);


	
	public static boolean isWeixinAccess(Map<String, String> requestDataMap) {
		String ua =requestDataMap.get("user-agent").toLowerCase();
		return isWeixinAccess(ua);
	}
	
	public static boolean isWeixinAccess(String userAgent) {
		if(logger.isDebugEnabled()){
			logger.debug("当前访问UA是" + userAgent);
		}
		if(userAgent == null){
			return false;
		}
		userAgent = userAgent.toLowerCase();
		if (userAgent.indexOf("micromessenger") > 0) {// 是微信浏览器
			return true;
		} 
		return false;
	}

}
