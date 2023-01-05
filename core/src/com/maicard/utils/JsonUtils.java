package com.maicard.utils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.maicard.core.constants.Constants;
import com.maicard.serializer.NoScienceFloatDeserializer;
import com.maicard.serializer.NoScienceLongDeserializer;
import com.maicard.views.JsonFilterView;

/**
 * 
 * 不再使用ThreadLocal保证线程安全，因为Jackson自身是线程安全, NetSnake, 2017-06-09<br>
 * 使用ThreadLocal保证线程安全,NetSnake,2016-04-10<br>
 * 
 * 对map中指定的明文数据进行加密，再把加密后的密文放回map中
 *
 *
 * @author NetSnake
 * @date 2015年11月17日
 *
 */
public class JsonUtils {

	protected static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
    
    private static ObjectMapper defaultOm = null;
    private static ObjectMapper serializeInstance = null;


    /**
     * 返回一个输出级别为NON_NULL的ObjectMapper实例
     */
	public static ObjectMapper getInstance(){
        if(defaultOm == null){  
        	defaultOm = init();
        }  
		return defaultOm;
	}

	public static ObjectMapper getSerializeInstance(){
		if(serializeInstance == null){
			serializeInstance = initSerialize();
		}
		return serializeInstance;
	}

	/**
     * 返回一个输出级别为NON_DEFAULT的ObjectMapper实例
     */
	public static ObjectMapper getNoDefaultValueInstance(){
		return getInstance();
		/*
        if(nonDefaultOm == null){
        	nonDefaultOm = init();
        	nonDefaultOm.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        }
		return nonDefaultOm;*/
	}
	
	@SuppressWarnings("deprecation")
	private static ObjectMapper init(){
		final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);
		ObjectMapper om = new ObjectMapper();
		om.setDateFormat(sdf);
		//貌似无效
		//om.getFactory().enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
		om.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
		om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		om.setTimeZone(TimeZone.getDefault());
		//om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		SimpleModule module = new SimpleModule("DoubleSerializer");

	    module.addSerializer(Float.class, new NoScienceFloatDeserializer());
	    module.addSerializer(Long.class, new NoScienceLongDeserializer());
	  //  module.addSerializer(HashMap<?,?>.class,new ExtraDataDeserializer());

		om.registerModule(module);
		return om;
	}

	private static ObjectMapper initSerialize(){

		ObjectMapper om = init();
		om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		return om;
	}


	public static void prettyPrint(String jsonStr){
		System.out.println(format(jsonStr));
	}


	public static String format(String jsonStr) {
		if (null == jsonStr || "".equals(jsonStr)) return "";
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		char current = '\0';
		int indent = 0;
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
			switch (current) {
			case '{':
			case '[':
				sb.append(current);
				sb.append('\n');
				indent++;
				addIndentBlank(sb, indent);
				break;
			case '}':
			case ']':
				sb.append('\n');
				indent--;
				addIndentBlank(sb, indent);
				sb.append(current);
				break;
			case ',':
				sb.append(current);
				if (last != '\\') {
					sb.append('\n');
					addIndentBlank(sb, indent);
				}
				break;
			default:
				sb.append(current);
			}
		}

		return sb.toString();
	}


	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append('\t');
		}
	}

	/**
	 * 使用NON_DEFAULT输出一个对象的JSON字符串<br>
	 * 使用标准JsonFilterView进行过滤
	 * 
	 * @param object
	 */
	public static String toStringApi(Object object) {
		
		try {
			return getNoDefaultValueInstance().writerWithView(JsonFilterView.Partner.class).writeValueAsString(object);
			//return getNoDefaultValueInstance().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 使用NON_DEFAULT输出一个对象的JSON字符串<br>
	 * 不使用View过滤
	 * 
	 * @param object
	 */
	public static String toStringFull(Object object) {
		
		try {
			return getNoDefaultValueInstance().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}



