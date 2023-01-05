package com.maicard.utils;

import com.maicard.core.constants.Constants;
import com.maicard.security.entity.SecurityLevel;

public class SecurityLevelUtils {

	private static int securityLevel = -1;
	
	public static int getSecurityLevel(){
		if(securityLevel == -1){
			securityLevel = NumericUtils.getNumeric(System.getProperty(Constants.EIS_SECURITY_ENV_NAME));
			System.out.println("---------------------- EIS Security Level:" + securityLevel + "---------------------------");
		}
		return securityLevel;
	}
	
	public static String getConfig(SecurityLevel securityLevel, String configName){
		if(securityLevel == null){
			return null;
		}
		if(securityLevel.getData() == null){
			return null;
		}
		if(securityLevel.getData().get(configName) == null){
			return null;
		}
		
		return securityLevel.getExtra(configName);
	}
	



}
