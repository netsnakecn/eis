package com.maicard.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

	public static String trim(String src){
		String re = "<[^>]+>";
		Pattern pattern = Pattern.compile(re, Pattern.CASE_INSENSITIVE);  
		Matcher matcher = pattern.matcher(src);  
		return matcher.replaceAll("");		
	}

	public static String trimWithBr(String src){

		String re = "<br>|<br/>|<br />";
		Pattern p_style = Pattern.compile(re, Pattern.CASE_INSENSITIVE);  
		Matcher m_style = p_style.matcher(src);  
		String  dst = m_style.replaceAll("\\$\\$\\$"); // 过滤style标签  
		re = "<[^>]+>";
		Pattern pattern = Pattern.compile(re, Pattern.CASE_INSENSITIVE);  
		Matcher matcher = pattern.matcher(dst);  
		return matcher.replaceAll("").replaceAll("\\$\\$\\$", "<br>");
	}

}
