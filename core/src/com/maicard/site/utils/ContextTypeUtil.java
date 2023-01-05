package com.maicard.site.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

import com.maicard.core.constants.ContextType;


public class ContextTypeUtil {
	
	public static String getContextType(HttpServletRequest request){
		
		String ua = request.getHeader("user-agent");
		
		boolean isWeixin = AgentUtils.isWeixinAccess(ua);
		if(isWeixin){
			return ContextType.WEIXIN.toString();
		}
		if(ua != null && ua.toLowerCase().startsWith("unity")){
			return ContextType.APP.toString();
		}
		Device device = DeviceUtils.getCurrentDevice(request);
		if(device == null){
			return ContextType.PC.toString();
		}
		if(device.isMobile() || device.isTablet()){
			return ContextType.WAP.toString();
		}
		
		return ContextType.PC.toString();
	}

	
	public static boolean isMatchedContextType(HttpServletRequest request, String contextType) {
		String currentContextType = getContextType(request);
		if(currentContextType.equalsIgnoreCase(contextType)){
			return true;
		}
		//微信也认为是WAP的一种
		if(currentContextType.equalsIgnoreCase(ContextType.WEIXIN.toString()) && contextType.equalsIgnoreCase(ContextType.WAP.toString())){
			return true;
		}
		return false;
	}

}
