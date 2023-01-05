package com.maicard.tx.utils;

public class AreaTrimUtils {
	
	public static String trimArea(String area){
		if(area == null){
			return null;
		}
		area = area.trim();
		if(area.startsWith("西藏")){
			return "西藏";
		}
		if(area.startsWith("青海")){
			return "青海";
		}
		if(area.startsWith("宁夏")){
			return "宁夏";
		}
		if(area.startsWith("新疆")){
			return "新疆";
		}
		return area.replaceAll("(省|市)$", "").trim();
	}
	
}
