package com.maicard.tx.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;
import com.maicard.money.entity.Money;
 

/**
 * 费用减免或增加的配置
 *
 *
 * @author NetSnake
 * @date 2016年4月26日
 *
 */
public class FeeAdjust extends BaseEntity {
	
	private static final long serialVersionUID = -6815859154742363184L;

		
	private String priceObjectType;		//价格对象类型，对应于Price中的objectType
	
	private long priceObjectId;			//价格的对象ID，对应于Price中的objectId
	
	private String priceType;	//价格类型，@see com.maicard.standard.PriceType
	
	private String priceIdentify;		//特殊价格的识别，比如某个具体活动需要特殊价格，格式activity#5标记为是给5#活动指定的价格

	
		
	private String toArea;			//配送起始地
	
	private String fromArea;		//配送目的地
	
	private long deliveryCompanyId;		//配送公司
	
	private Date beginTime;			//起始时间
	
	private Date endTime;			//截止时间
	
	private Money fee;			//减免价格
	
	private boolean clearDeliveryFee;			//完全免除运费，这时候无论减免价格是多少，都将不收取快递费
	
	private boolean exclusive;		//不与其他减免一起参加
	
	private String identify;		//识别码
	
 


	public String getToArea() {
		return toArea;
	}

	public void setToArea(String toArea) {
		this.toArea = toArea;
	}

	public String getFromArea() {
		return fromArea;
	}

	public void setFromArea(String fromArea) {
		this.fromArea = fromArea;
	}

	public long getDeliveryCompanyId() {
		return deliveryCompanyId;
	}

	public void setDeliveryCompanyId(long deliveryCompanyId) {
		this.deliveryCompanyId = deliveryCompanyId;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}


	public String getIdentify() {
		return identify;
	}

	public void setIdentify(String identify) {
		this.identify = identify;
	}
	
	public Money getFee() {
		return fee;
	}

	public void setFee(Money fee) {
		this.fee = fee;
	}

	public String getPriceObjectType() {
		return priceObjectType;
	}

	public void setPriceObjectType(String priceObjectType) {
		this.priceObjectType = priceObjectType;
	}

	public long getPriceObjectId() {
		return priceObjectId;
	}

	public void setPriceObjectId(long priceObjectId) {
		this.priceObjectId = priceObjectId;
	}

	public String getPriceType() {
		return priceType;
	}

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	public String getPriceIdentify() {
		return priceIdentify;
	}

	public void setPriceIdentify(String priceIdentify) {
		this.priceIdentify = priceIdentify;
	}

	public boolean isClearDeliveryFee() {
		return clearDeliveryFee;
	}

	public void setClearDeliveryFee(boolean clearDeliveryFee) {
		this.clearDeliveryFee = clearDeliveryFee;
	}

}
