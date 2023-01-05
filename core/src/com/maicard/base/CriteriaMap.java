package com.maicard.base;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.misc.ThreadHolder;
import com.maicard.utils.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CriteriaMap extends LinkedHashMap<String,Object>{

	protected  Logger logger = LoggerFactory.getLogger(this.getClass());


	public String getCacheType() {
		return cacheType;
	}

	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	private String cacheType;

	public CriteriaMap setCacheType(Class<?> clazz){
		this.cacheType = StringUtils.uncapitalize(clazz.getSimpleName());
		return this;
	}

	static String[] arrayAttr = new String[]{"currentStatus"};


	public static CriteriaMap create() {
		return new CriteriaMap();
	}
	public static CriteriaMap create(long ownerId) {
		CriteriaMap map = create();
		map.put("ownerId",ownerId);
		return map;
	}


	public CriteriaMap clone() {
		return (CriteriaMap)super.clone();
	}
	
	
	public CriteriaMap setPaging(int page, int rows) {
		this.put("page", page);
		this.put("rows", rows);
		return this;
	}

	@JsonIgnore
	public Map<String,String> getOrder(){
		Map<String,String> orderMap = new LinkedHashMap<>();
		if(this.containsKey("orderBy")){
			String order = this.get("orderBy");
			for(String v : order.split(",")){
				String[] vArray = v.split("\\s+");
				if(vArray.length > 1){
					orderMap.put(StringTools.underlineToCamel(vArray[0],true), vArray[1]);
				} else {
					orderMap.put(StringTools.underlineToCamel(v,true),"ASC");
				}
			}
		}
		return orderMap;

	}


	public CriteriaMap setOrder(String orderBy, Class<?> clazz){

		log.info("对对象:" + clazz.getName() + "排序:" + orderBy);
		if(StringUtils.isBlank(orderBy)){
			return this;
		}

		Set<String> names = ClassUtils.getPropertyNames(clazz);

		//System.out.println("Name set:" + JsonUtils.toStringFull(names));
		String[] array = orderBy.split("\\s+|,");
		System.out.println(JsonUtils.toStringFull(array));
		StringBuffer sb = new StringBuffer();
		for(String s : array){
			if(StringUtils.isBlank(s)){
				continue;
			}
			s = s.trim();
			if(s.startsWith("+") || s.startsWith("-")){
				//以+/-形式提交的参数
				String o = null;
				if(s.startsWith("-")){
					o = "DESC";
				} else {
					o = "ASC";
				}
				String column = s.substring(1);
				if(!names.contains(column)){
					log.error("对象不包含属性:" + column + "，对象属性列表:" + JsonUtils.toStringFull(names));
					throw new EisException(EisError.DATA_ILLEGAL.getId(),"");
				}
				column = StringTools.toUnderLine(column);
				sb.append(column).append(" ").append(o);
				continue;
			}
			if(s.equalsIgnoreCase("ASC") || s.equalsIgnoreCase("DESC") || s.equalsIgnoreCase(",")){
				sb.append(s).append(",");
				continue;
			}
			 //System.out.println("Check:" + s);
			if(!names.contains(s)){
				log.error("对象不包含属性:" + s + "，对象属性列表:" + JsonUtils.toStringFull(names));
				throw new EisException(EisError.DATA_ILLEGAL.getId(),"");
			}
			s = StringTools.toUnderLine(s);
			sb.append(s).append(" ");
		}
		//this.remove(orderBy);
		super.put("orderBy",sb.toString().replaceAll(",$",""));
		return this;

	}

	public CriteriaMap putArray(String key, Object...objects) {
		if(objects == null || objects.length < 1 || objects[0] == null) {
			remove(key);
			return this;
		}
		if(key.equalsIgnoreCase("order") || key.equalsIgnoreCase("orderBy")){
			throw new EisException(EisError.DATA_ILLEGAL.id,"Illegal property");
		}
		if(objects.length == 1) {
			Object o1 = objects[0];
			//System.out.println(o1.getClass());
			if(o1.getClass().isArray()) {
				put(key, o1);
				return this;
			}
		}
		put(key, objects);
		return this;
	}
	@Override
	public CriteriaMap put(String key, Object value) {
		if(key == null) {
			return this;
		}
		if(value == null) {
			return this;
		}
		String v = value.toString().trim();
		if(v.equals("")) {
			return this;
		}

		if(key.trim().equalsIgnoreCase("order") || key.trim().equalsIgnoreCase("orderBy")){
			throw new EisException(EisError.DATA_ILLEGAL.id,"Illegal property");
		}
		super.put(key, value);
		return this;
	}

	public String getStringValue(String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return null;
		}
		Object value = super.get(key.trim());
		if(value == null || value.toString().trim().equals("")) {
			return null;
		} else {
			return value.toString().trim();
		}
	}

	public long getLongValue(String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return 0;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return 0;
		} 

		return NumericUtils.parseLong(value);
	}
	
	public float getFloatValue( String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return 0;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return 0;
		} 

		return NumericUtils.parseFloat(value);
	}

	public double getDoubleValue( String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return 0;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return 0;
		} 

		return NumericUtils.parseDouble(value);
	}

	
	public int getIntValue( String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return 0;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return 0;
		} 

		return NumericUtils.parseInt(value);
	}

	public  boolean getBooleanValue(String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return false;
		}
		Object value = super.get(key.trim());
		if(value != null && value.toString().trim().equalsIgnoreCase("true")) {
			return true;
		} 
		return false;
	}

	@SuppressWarnings("unchecked")
	public <T>T get(String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return null;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return null;
		} 
        
		try {
			return (T)value;
		}catch(Exception e) {
			logger.error("无法将类型:{}转换为所需类型:{}", value.getClass().getName(), e.getMessage());
		}
		return null;
	}
	public static CriteriaMap create(Map<String, Object> params) {
		CriteriaMap map = CriteriaMap.create();
		for(String k : params.keySet()){
			if(k.trim().equalsIgnoreCase("order") || k.trim().equalsIgnoreCase("orderBy")){
				throw new EisException(EisError.DATA_ILLEGAL.id,"Illegal property");
			}

			if(k.equalsIgnoreCase("currentStatus")){
				params.put(k, StringTools.str2long(params.get(k).toString()));
			}
		}

		map.putAll(params);
		return map;
	}
/*

	public CriteriaMap toUnderLine() {
		Map<String, Object> param = new LinkedHashMap<>(this);
		this.clear();
		for(Map.Entry<String,Object> entry : param.entrySet()){
			 String k = StringTools.toUnderLine(entry.getKey());
			 this.put(k,entry.getValue());
		}
		return this;
	}
*/


	public CriteriaMap init() {
		long oid = this.getLongValue("ownerId");
		this.clear();
		this.put("ownerId", oid);
		return this;
	}

	public CriteriaMap fixToArray(String paramName) {
		Object paramObj = this.get(paramName);
		if(paramObj == null) {
			return this;
		}
		if(paramObj.getClass().isArray()) {
			return this;
		}
		String v = this.getStringValue(paramName);
		this.put(paramName, StringTools.str2long(v));
		return this;
	}
	
	public CriteriaMap fixBool(String paramName) {
		Object valueObj = this.get(paramName);
		if(valueObj == null) {
			return this;
		}
		if(valueObj instanceof Boolean) {
			return this;
		}
		String valueStr = valueObj.toString();
		if(valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
			this.put(paramName, Boolean.valueOf(valueStr));
		}
		return this;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public Date getDate(String key) {
		if(this.size() < 1 || !this.containsKey(key.trim())) {
			return null;
		}
		Object value = super.get(key.trim());
		if(value == null) {
			return null;
		} 
        if(value instanceof Date) {
        	return (Date)value;
        }
		try {
			return ThreadHolder.defaultTimeFormatterHolder.get().parse(value.toString());
		}catch(Exception e) {
			logger.error("无法将类型:{}转换为所需类型:{}", value.getClass().getName(), e.getMessage());
		}
		return null;
	}


	public static  void toOnPage(CriteriaMap paramMap){
		boolean noPaging = false;
		int limits = paramMap.getIntValue("limits");
		int starts = paramMap.getIntValue("starts");
		if(limits <= 0 || starts <= 0) {
			if(!noPaging) {
				int rows = paramMap.getIntValue("rows");
				if(rows <= 0){
					rows = Constants.DEFAULT_PARTNER_ROWS_PER_PAGE;
				}
				int page = paramMap.getIntValue("page");
				if(page <= 0){
					page = 1;
				}
				PagingUtils.paging(paramMap, rows, page);
			} else {
				PagingUtils.paging(paramMap, Constants.MAX_ROW_LIMIT);
			}
		}

	}

	public static void fixArray(CriteriaMap paramMap) {
		for(String paramName : arrayAttr) {
			paramMap.fixToArray(paramName);
		}
	}
}
