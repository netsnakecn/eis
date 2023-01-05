package com.maicard.base;

import org.apache.commons.lang.StringUtils;

import com.maicard.core.constants.Constants;


public class ImplNameTranslate {
	
	public static String translate(String orgName){
		//Remove hotfixservice prefix hfs
		return StringUtils.uncapitalize(orgName.replaceFirst("hfs", "").replaceFirst("Hfs", "")).replace(Constants.implBeanNameSuffix, "");	
	}
	
	public static void main(String[] argv) {
		String name = "HfsNameImpl";
		System.out.println(translate(name));
	}

}
