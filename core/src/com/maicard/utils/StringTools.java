package com.maicard.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.constants.Constants;
import com.maicard.misc.ThreadHolder;
import org.springframework.util.ResourceUtils;


public class StringTools {

	protected static final Logger logger = LoggerFactory.getLogger(StringTools.class);

	private static Pattern humpPattern = Pattern.compile("[A-Z]");

	public static String createCacheKey(Class<?> clazz) {
		return "QSS_CACHE_" + clazz.getSimpleName();
	}
	public static String map2LinkString(Map<String,?> map) {
		ArrayList<String> mapKeys = new ArrayList<String>(map.keySet());
		Collections.sort(mapKeys);
		StringBuilder link = new StringBuilder();
		boolean first = true;
		//for_map_keys:
		for(String key: mapKeys) {
			Object value = map.get(key);
			if(value == null || StringUtils.isEmpty(value.toString())){
				continue;
			}
			if (!first) link.append("&");
			link.append(key).append("=").append(value.toString());
			if (first) first = false;
		}
		return link.toString();
	}

	public static String readClassPathContent(final String configName) {
		//final String configName = "classpath:pconfig/user_extra_type.json";

		String text = null;
		try {
			File file = ResourceUtils.getFile(configName);
			text = FileUtils.readFileToString(file,Constants.DEFAULT_CHARSET);
			logger.info("读取配置文件:" + file.getName() + "=>" + text);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	/**
	 * 把element加入src，然后返回一个以,分割的字符串
	 * 如果src中已经有了element，则不会重复
	 * 
	 * @param src
	 * @param element
	 * @return
	 */
	public static String addElementNoDuplicate(String src, String element){
		if(src == null){
			return element;
		}
		if(element == null){
			return src;
		}
		StringBuilder sb = new StringBuilder();
		String[] data = src.split(",");
		if(data == null || data.length < 1){
			return element;
		}
		Set<String> dataSet = new HashSet<String>();
		for(String d : data){
			dataSet.add(d);
		}
		dataSet.add(element);
		for(String d : dataSet){
			sb.append(d).append(",");
		}
		return sb.toString().replaceAll(",$", "");
	}

	/**
	 * 把element加入src，然后返回一个以,分割的字符串
	 * 如果src中已经有了element，则使用value替换以#分割的值
	 * 
	 * @param src
	 * @param element
	 * @param value
	 * @return
	 */
	public static String addElementNoDuplicate(String src, String element, String value){
		String singalValue = element + "#" + value;
		if(src == null){
			return singalValue;
		}

		StringBuilder sb = new StringBuilder();
		String[] data = src.split(",");
		if(data == null || data.length < 1){
			return singalValue;
		}
		Set<String> dataSet = new HashSet<String>();
		boolean isReplace = false;
		for(String d : data){
			if(d.startsWith(element + "#")){
				isReplace = true;
				dataSet.add(singalValue);
			} else {
				dataSet.add(d);

			}
		}
		if(!isReplace){
			dataSet.add(singalValue);
		}
		for(String d : dataSet){
			sb.append(d).append(",");
		}
		return sb.toString().replaceAll(",$", "");
	}

	public static Set<String> getSetFromString(String[] src) {

		if(src == null || src.length < 1){
			return null;
		}
		Set<String> set = new HashSet<String>();
		for(String d : src){
			set.add(d.trim());
		}
		return set;
	}

	public static Set<String> getSetFromString(String src, String split) {
		Set<String> set = new HashSet<String>();

		if(StringUtils.isBlank(src)){
			return set;
		}

		String[] data = src.split(split);		

		if(data == null || data.length < 1){
			return set;
		}
		for(String d : data){
			set.add(d.trim());
		}
		return set;
	}

	public static Set<Integer> getIntSetFromString(String src, String split) {
		Set<Integer> set = new HashSet<Integer>();

		if(StringUtils.isBlank(src)){
			return set;
		}

		String[] data = src.split(split);		

		if(data == null || data.length < 1){
			return set;
		}
		for(String d : data){
			if(NumericUtils.isIntNumber(d)){
				set.add(NumericUtils.parseInt(d));
			}
		}
		return set;
	}

	public static boolean isMobilePhone(String src) {
		if(src == null){
			return false;
		}
		if(src.length() != 11){
			return false;
		}
		if(!src.startsWith("1")){
			return false;
		}
		if(!src.startsWith("13") && !src.startsWith("15") && !src.startsWith("17")){
			return false;
		}
		return true;

	}

	public static String mergeArray(String[] array) {
		if(array == null || array.length < 1){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(String data : array){
			sb.append(data).append(",");
		}
		return sb.toString().replaceAll(",$", "");
	}

	public static String ts2String(long time){
		if(time < 1510388920693L){
			return String.valueOf(time);
		}
		//return new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT).format(new Date(time));
		return ThreadHolder.defaultTimeFormatterHolder.get().format(new Date(time));
	}

	public static String time2String(Date time) {
		if(time == null){
			return null;
		}
		return ThreadHolder.defaultTimeFormatterHolder.get().format(time);
	}

	public static Date parseTime(String date){
		if(StringUtils.isBlank(date)){
			return null;
		}
		try{
			return new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT).parse(date);
		}catch(Exception e){

		}
		return null;
	}
	/**
	 * 过滤掉源字符串中的所有EMOJI表情字符
	 * 
	 *
	 * @author GHOST
	 * @date 2018-03-20
	 */
	public static String filterEmoji(String src){
		Pattern emoji = Pattern.compile ("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]" ,Pattern . UNICODE_CASE | Pattern . CASE_INSENSITIVE ) ;
		Matcher matcher = emoji.matcher(src);

		return matcher.replaceAll("").trim();
	}

	/**
	 * 过滤掉源字符串中的所有中文字符
	 * 
	 *
	 * @author GHOST
	 * @date 2018-03-20
	 */
	public static String filterChinese(String src){
		Pattern pattern = Pattern.compile ("[(\\u4e00-\\u9fa5)]" ,Pattern . UNICODE_CASE | Pattern . CASE_INSENSITIVE ) ;
		Matcher matcher = pattern.matcher(src);
		return matcher.replaceAll("").trim();  
	}
	/*public static String concat(Object... src){
		StringBuffer sb = new StringBuffer();

		for(Object data : src){
			sb.append(data.toString());
		}
		return sb.toString();
	}*/

	/**
	 * 生成一个指定长度、纯数字的验证码
	 * @return
	 */
	public static String generateRandomCode(int size) {
		if(size < 1) {
			size = 6;
		}
		int basicNumber = (int)Math.pow(10, size);
		int basic2 = (int)Math.pow(10, size-1);
		System.out.println("basicNumber=" + basicNumber + ",basic2=" + basic2);
		Random random = new Random();
		String randomCode=String.valueOf(random.nextInt(basicNumber - 1)%(basicNumber - basic2) + basic2);
		return randomCode;
	}

	/**
	 * 返回文件的扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileExt(String fileName) {
		int offset = fileName.lastIndexOf(".");
		if(offset <= 0) {
			return "";
		} else {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		}
	}

	/**
	 * 逗号分隔的字符串转换为long数组
	 */
	public static long[] str2long(String src) {
		src = src.replaceAll("^,","").replaceAll(",$","");
		String[] data = src.split(",");
		int size = data.length;
		long[] num = new long[size];
		for(int i = 0; i < size; i++) {
			num[i] = NumericUtils.parseLong(data[i]);
		}
		return num;
		
	}

	public static String underlineToCamel(String line,boolean smallCamel ){
		if(line==null||"".equals(line)){
			return "";
		}
		StringBuffer sb=new StringBuffer();
		Pattern pattern=Pattern.compile("([A-Za-z\\d]+)(_)?");
		Matcher matcher=pattern.matcher(line);
		while(matcher.find()){
			String word=matcher.group();
			sb.append(smallCamel&&matcher.start()==0?Character.toLowerCase(word.charAt(0)):Character.toUpperCase(word.charAt(0)));
			int index=word.lastIndexOf('_');
			if(index>0){
				sb.append(word.substring(1, index).toLowerCase());
			}else{
				sb.append(word.substring(1).toLowerCase());
			}
		}
		return sb.toString();
	}

    public static String toUnderLine(String v) {
		Matcher matcher = humpPattern.matcher(v);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "_" + matcher.group(0).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString().toLowerCase();

    }

    public static String mask(String src) {
		if(src == null){
			return src;
		}

		if(src.length() > 8){
			return src.substring(0,4) + "****" + src.substring(src.length()-4);
		}
		if(src.length() > 2){
			return src.substring(0,1) + "*" + src.substring(src.length()-1);
		}
		return src.substring(0,1) + "*";
    }

    public static boolean isPositive(String v) {
		if(v == null){
			return false;
		}
		v = v.trim();
		if(v.equalsIgnoreCase("1")){
			return true;
		}
		if(v.equalsIgnoreCase("true")){
			return true;
		}
		return false;

    }

}
