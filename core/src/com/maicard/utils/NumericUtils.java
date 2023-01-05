package com.maicard.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.maicard.core.constants.Constants;


/**
 * 数字工具
 *
 *
 * @author NetSnake
 * @date 2015年11月28日
 *
 */
public class NumericUtils {

	public static boolean isNumeric(Object obj){
		if(obj == null ){
			return false;
		}

		if(obj instanceof Integer || obj instanceof Float || obj instanceof Double || obj instanceof Long){
			return true;
		}

		if(obj.toString().trim().equals("")){
			return false;
		}
		String src = obj.toString().trim();
		if(src.startsWith("-") || src.startsWith("\\+")){
			src = src.substring(1);
		}
		int pointCount = 0;
		for(int i = 0, length = src.length(); i < length; i++){
			if(src.charAt(i) < 48 || src.charAt(i) > 58){				
				if(src.charAt(i) == '.'){
					pointCount++;
				} else {
					return false;
				}
			}
			if(pointCount > 1){
				return false;
			}
		}	
		return true;
	}

	/**
	 * 数据是否一个浮点数
	 * 
	 * 
	 * @param src
	 * @return
	 */
	public static boolean isFloatNumber(String src){
		if(src == null || src.trim().equals("")){
			return false;
		}
		src = src.trim();
		if(src.startsWith("-") || src.startsWith("\\+")){
			src = src.substring(1);
		}
		int pointCount = 0;
		for(int i = 0, length = src.length(); i < length; i++){
			if(src.charAt(i) < 48 || src.charAt(i) > 58){				
				if(src.charAt(i) == '.'){
					pointCount++;
				} else {
					return false;
				}
			}			
		}	
		if(pointCount != 1){
			return false;
		}
		return true;
	}

	public static boolean isIntNumber(String src){
		if(src == null || src.trim().equals("")){
			return false;
		}
		src = src.trim();
		if(src.startsWith("-") || src.startsWith("\\+")){
			src = src.substring(1);
		}
		if(src.length() > 10){
			return false;
		}
		for(int i = 0, length = src.length(); i < length; i++){
			if(src.charAt(i) < 48 || src.charAt(i) > 58){	
				return false;
			}
		}	
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T>T getNumeric(Object obj){
		if(!isNumeric(obj)){
			return  (T)new Integer(0);
		}
		String src = obj.toString().trim();
		if(src.indexOf(".") > 0){
			return (T)new Float(src);
		}
		if(src.replaceAll("-", "").replaceAll("\\+","").replaceAll("\\.", "").length() > 11){
			return (T)new Long(src);
		}
		return (T)new Integer(src);

	}

	public static int parseInt(Object src) {
		if(src == null){
			return 0;
		}
		String number = src.toString();

		if(!isIntNumber(number)){
			return 0;
		}
		if(number.indexOf(".") > 0){
			number = number.replaceAll("\\.\\d+", "");
		}
		return Integer.parseInt(number.trim());
	}

	public static long parseLong(Object src) {
		if(src == null){
			return 0;
		}
		String number = src.toString();
		if(!isNumeric(number)){
			return 0;
		}
		if(number.indexOf(".") > 0){
			number = number.replaceAll("\\.\\d+", "");
		}
		if(number.length() > 19){
			return 0;
		}
		return Long.parseLong(number.trim());
	}

	public static float parseFloat(Object src) {
		if(src == null){
			return 0;
		}
		String number = src.toString();
		if(!isNumeric(number)){
			return 0;
		}
		if(number.indexOf(".") < 0){
			number += ".00";
		}
		return Float.parseFloat(number.trim());	
	}
	
	public static double parseDouble(Object src) {
		if(src == null){
			return 0;
		}
		String number = src.toString();
		if(!isNumeric(number)){
			return 0;
		}
		if(number.indexOf(".") < 0){
			number += ".00";
		}
		return Double.parseDouble(number.trim());	
	}


	public static int float2int(float floatValue) {
		return new Float(floatValue).intValue();
	}

	/**
	 * 使用默认配置四舍五入
	 */
	public static double round(double src) {
		return round(src, Constants.MONEY_ROUND_LENGTH, Constants.MONEY_ROUND_TYPE);
	}
	
	/**
	 * 使用指定配置四舍五入
	 */
	public static double round(double src, int roundLength, int roundType) {
		BigDecimal bd = new BigDecimal(src);
		bd = bd.setScale(roundLength, roundType);
		return bd.doubleValue();
	}

	/**
	 * 把Integer列表转换为int数组
	 * @return int数组
	 */
	public static int[] list2Array(List<Integer> list) {
		int length = list.size();
		int[] array = new int[length];
		for(int i = 0; i < length; i++){
			array[i] = list.get(i);
		}
		return array;
	}
	
	/**
	 * 把Long列表转换为long数组
	 */
	public static long[] longList2Array(List<Long> list) {
		int length = list.size();
		long[] array = new long[length];
		for(int i = 0; i < length; i++){
			array[i] = list.get(i);
		}
		return array;
	}
	
	/**
	 * 把Long set转换为long数组
	 */
	public static long[] longSet2Array(Set<Long> list) {
		int length = list.size();
		long[] array = new long[length];
		int i = 0;
		for(long x : list){
			array[i] = x;
			i++;
		}
		return array;
	}
	
	/**
	 * 把int数组转换为Integer列表
	 * @param int数组 
	 * @return Integer List
	 */
	public static List<Integer> array2List(int[] array) {
		List<Integer> list = new ArrayList<Integer>();

		if(array == null || array.length < 1){
			return list;
		}
		for(int value : array){
			list.add(value);
		}
		return list;
	}

	public static String format(float src) {
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(src);
	}

	


	/*public static byte[] listToByte(List<Integer> newCardList) {
		int length = newCardList.size();
		byte[] array = new byte[length];
		for(int i = 0; i < newCardList.size(); i++){
			array[i] = newCardList.get(i).byteValue();
		}
		return array;
		
	}*/
}
