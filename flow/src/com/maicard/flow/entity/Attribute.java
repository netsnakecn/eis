package com.maicard.flow.entity;

import java.io.Serializable;
import java.util.Map;


/**
 * 用于在工作流过程中，控制对象的属性
 * 如何操作对象的这些属性
 *
 *
 * @author NetSnake
 * @date 2016年6月9日
 *
 */
public class Attribute implements Serializable{

	public static final String ATTRIBUTE_NATIVE = "native";
	public static final String ATTRIBUTE_EXTRA = "extra";
	

	private static final long serialVersionUID = 8363142954880669295L;

	private String columnType = ATTRIBUTE_NATIVE;
	

	private String name;

	private boolean readonly;			//是否不可写入

	private int hidden = 0;			//是否显示：0显示，1不显示，作为隐藏字段，2不输出

	private boolean required;			//是否必须输入
	
	
	private String inputMethod;			//输入方式

	private Map<String,String> validValue;			//有效的值，如果为空则为任意值

	/**
	 * 在页面中显示为哪个<spring:message>前缀
	 */
	private String msgPrefix;

	private int weight;					//数字越大越靠前

	public Attribute(){

	}

	public Attribute(String type, String name, String inputMethod, boolean readonly, int hidden, boolean required, Map<String,String>validValue){
		this.columnType = type;
		this.name = name;
		this.inputMethod = inputMethod;
		this.readonly = readonly;
		this.hidden = hidden;
		this.required = required;
		this.validValue = validValue;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}


	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
 
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getInputMethod() {
		return inputMethod;
	}

	public void setInputMethod(String inputMethod) {
		this.inputMethod = inputMethod;
	} 

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getHidden() {
		return hidden;
	}

	public void setHidden(int hidden) {
		this.hidden = hidden;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMsgPrefix() {
		return msgPrefix;
	}

	public void setMsgPrefix(String msgPrefix) {
		this.msgPrefix = msgPrefix;
	}

	public Map<String, String> getValidValue() {
		return validValue;
	}

	public void setValidValue(Map<String, String> validValue) {
		this.validValue = validValue;
	}
}
