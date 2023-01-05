package com.maicard.flow.entity;

import com.maicard.core.entity.BaseEntity;
/**
 * 进行下一步操作的条件
 * 即某个属性的值匹配则进入指定的工作步骤
 * 
 * @author NetSnake
 * @date 2016-06-23
 */
public class StepCondition extends BaseEntity{

	private static final long serialVersionUID = -904797004455088570L;
	
	private String columnType = Attribute.ATTRIBUTE_NATIVE;


	private int weight;		//优先级
	
	private String attributeName;		//匹配条件的属性名
	
	private String attributeValue;		//符合条件的值
	
	private int toStep;				//符合条件则前往哪个步骤
	
	private boolean last;			//本条件是否是最后一个条件，是则不再进行后续判断
	
	public StepCondition(){
		
	}
	
	public StepCondition(int weight, String attributeName, String attributeValue, int toStep, boolean last){
		this.weight = weight;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.toStep = toStep;
		this.last = last;
		
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public int getToStep() {
		return toStep;
	}

	public void setToStep(int toStep) {
		this.toStep = toStep;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	

}
