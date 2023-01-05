package com.maicard.money.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;


public class Settlement extends BaseEntity  {

	private static final long serialVersionUID = 7732346336737289793L;
	
	private Date billingBeginTime;	//结算开始时间
	private Date billingEndTime;	//结算结束时间
	private Date billingHandlerTime; //结算处理时间
	private long uuid;			//结算账户UUID
	private Float tradeMoney;		//结算面额
	private Float tradeCount;		//交易次数
	private Float totalMoney;		
	private Float totalCount;		
	private Float commission;		//佣金，手续费
	private Float settlementMoney;		//实际结算金额
	private String objectType;		//结算对应的业务类型，business:业务
	private Integer objectId;		//结算对应的业务ID
	private Float sharePercent;	//当前结算单用到的结算比例
	
	
	//////////// 非持久化属性
	private String billingUser;	//结算用户名
	public Float getTradeCount() {
		return tradeCount;
	}

	public void setTradeCount(Float tradeCount) {
		this.tradeCount = tradeCount;
	}

	public Float getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(Float totalMoney) {
		this.totalMoney = totalMoney;
	}

	public Float getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Float totalCount) {
		this.totalCount = totalCount;
	}
	

	

	public Date getBillingBeginTime() {
		return billingBeginTime;
	}

	public Float getTradeMoney() {
		return tradeMoney;
	}

	public void setTradeMoney(Float tradeMoney) {
		this.tradeMoney = tradeMoney;
	}


	public Float getSettlementMoney() {
		return settlementMoney;
	}

	public void setSettlementMoney(Float settlementMoney) {
		this.settlementMoney = settlementMoney;
	}

	public void setBillingBeginTime(Date billingBeginTime) {
		this.billingBeginTime = billingBeginTime;
	}

	public Date getBillingEndTime() {
		return billingEndTime;
	}

	public void setBillingEndTime(Date billingEndTime) {
		this.billingEndTime = billingEndTime;
	}

	public Date getBillingHandlerTime() {
		return billingHandlerTime;
	}

	public void setBillingHandlerTime(Date billingHandlerTime) {
		this.billingHandlerTime = billingHandlerTime;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}



	public Float getCommission() {
		return commission;
	}

	public void setCommission(Float commission) {
		this.commission = commission;
	}



	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public Float getSharePercent() {
		return sharePercent;
	}

	public void setSharePercent(Float sharePercent) {
		this.sharePercent = sharePercent;
	}

	public String getBillingUser() {
		return billingUser;
	}

	public void setBillingUser(String billingUser) {
		this.billingUser = billingUser;
	}

 

	@Override
	public Settlement clone() {
		try{
			return (Settlement)super.clone();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


}
