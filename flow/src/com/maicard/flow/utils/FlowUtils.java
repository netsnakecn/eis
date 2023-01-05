package com.maicard.flow.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.maicard.flow.entity.Attribute;
import com.maicard.site.annotation.InputLevel;
import com.maicard.views.JsonFilterView.Partner;

public class FlowUtils {
	@SuppressWarnings("serial")
	public static Map<String,Attribute> getAttributeForInputLevel(Object targetObject, Class<Partner> class1) {
		LinkedHashMap<String,Attribute> map = new LinkedHashMap<String,Attribute>();
		for(Field field : targetObject.getClass().getDeclaredFields()){
			//logger.debug("XXXXXX" + field.getName() + "==============" + field.getType().getName());
			if(field.isAnnotationPresent(InputLevel.class)){
				InputLevel a = field.getAnnotation(InputLevel.class);
				if(a.value().getName().equals(class1.getName())){
					if(field.getType().getName().indexOf("Date") > 0){
						map.put(field.getName(), new Attribute(Attribute.ATTRIBUTE_NATIVE, field.getName(), "date", false, 0 ,false, new HashMap<String,String>(){}));	

					} else {
						map.put(field.getName(), new Attribute(Attribute.ATTRIBUTE_NATIVE, field.getName(), null, false, 0 ,false,  new HashMap<String,String>(){}));
					}
				} else {
					continue;
				}
			} else {
				continue;
				//map.put(filed.getName(), new Attribute(CommonStandard.COLUMN_TYPE_NATIVE, filed.getName(), null, false, 0 ,false, new String[]{}));	

			}
		}		
		return map;
	}
}
