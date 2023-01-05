package com.maicard.tx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryPrice extends BaseEntity{


	private static final long serialVersionUID = 7708221424202076371L;


	private long deliveryCompanyId;	//快递公司对应ID
	
	private String fromProvince;
	
	private String toProvince;

	private String fromArea;	//对应区域表ID

	private String toArea;	//对应区域表ID

	private String areaType;	//区域是省份还是地市

	private float basePrice;	//基础价格

	private int baseWeight;			//基础价格的重量

	private float additinalPrice;		//增加价格

	private int additinalWeightUnit;		//增加价格的重量单位

	private String identify;			//价格识别码，如果有活动或者其他优惠时，则这是对应的特殊价格
	
	private String memory;		

	public DeliveryPrice(){

	}
	public DeliveryPrice(long ownerId) {
		this.ownerId = ownerId;
	}
 

	public float getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(float basePrice) {
		this.basePrice = basePrice;
	}

	public int getBaseWeight() {
		return baseWeight;
	}

	public void setBaseWeight(int baseWeight) {
		this.baseWeight = baseWeight;
	}

	public float getAdditinalPrice() {
		return additinalPrice;
	}

	public void setAdditinalPrice(float additinalPrice) {
		this.additinalPrice = additinalPrice;
	}

	public int getAdditinalWeightUnit() {
		return additinalWeightUnit;
	}

	public void setAdditinalWeightUnit(int additinalWeightUnit) {
		this.additinalWeightUnit = additinalWeightUnit;
	}

	public String getFromArea() {
		return fromArea;
	}

	public void setFromArea(String fromArea) {
		this.fromArea = fromArea;
	}

	public String getToArea() {
		return toArea;
	}

	public void setToArea(String toArea) {
		this.toArea = toArea;
	}

	public String getAreaType() {
		return areaType;
	}

	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}

	public long getDeliveryCompanyId() {
		return deliveryCompanyId;
	}

	public void setDeliveryCompanyId(long deliveryCompanyId) {
		this.deliveryCompanyId = deliveryCompanyId;
	}

	public String getIdentify() {
		return identify;
	}

	public void setIdentify(String identify) {
		this.identify = identify;
	}
	public String getFromProvince() {
		return fromProvince;
	}
	public void setFromProvince(String fromProvince) {
		this.fromProvince = fromProvince;
	}
	public String getToProvince() {
		return toProvince;
	}
	public void setToProvince(String toProvince) {
		this.toProvince = toProvince;
	}
	public String getMemory() {
		return memory;
	}
	public void setMemory(String memory) {
		this.memory = memory;
	}





}
