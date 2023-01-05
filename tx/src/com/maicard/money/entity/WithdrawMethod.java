package com.maicard.money.entity;


import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.entity.BaseEntity;
import com.maicard.views.JsonFilterView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WithdrawMethod extends BaseEntity implements Cloneable {


	private static final long serialVersionUID = -7271651617034782741L;

	private int channelId;

	private String name;

	private String validTimeRange;

	private int flag;

	private String processClass;
	
	private int withdrawTypeId;
	
	private int weight;			//使用权重，越大越优先
	
	private int percent;		//相同条件下，使用该支付方式的比例, 最大为100
	
	/**
	 * 该提现通道对应的虚拟系统用户UUID，每次提现将从该虚拟账户中扣除资金
	 */
	private long referUuid;
	
	/**
	 * 支付通道的成本费率
	 */
    @JsonView({JsonFilterView.Full.class})
	private float commission;
	
	/**
	 * 成本类型
	 */
    @JsonView({JsonFilterView.Full.class})
	private String commissionType;

	protected  String currency;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public float getCommission() {
		return commission;
	}

	public void setCommission(float commission) {
		this.commission = commission;
	}

	public String getCommissionType() {
		return commissionType;
	}

	public void setCommissionType(String commissionType) {
		this.commissionType = commissionType;
	}

	private String withdrawMode;
		
	
	@Override
	public WithdrawMethod clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (WithdrawMethod)in.readObject();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public WithdrawMethod() {
	}
 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public String getValidTimeRange() {
		return validTimeRange;
	}

	public void setValidTimeRange(String validTimeRange) {
		this.validTimeRange = validTimeRange;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)id;

		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WithdrawMethod other = (WithdrawMethod) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"withdrawMethodId=" + "'" + id + "'" + 
			"name=" + "'" + name + "'" + 
			"withdrawTypeId=" + "'" + withdrawTypeId + "'" + 
			"processClass=" + "'" + processClass + "'" + 
			"weight=" + "'" + weight + "'" + 
			"percent=" + "'" + percent + "'" +
			"ownerId=" + "'" + ownerId + "'" + 
			"currentStatus=" + "'" + currentStatus + "'" +
			")";
	}
	
	


	public String getProcessClass() {
		return processClass;
	}

	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}


	public int getWithdrawTypeId() {
		return withdrawTypeId;
	}

	public void setWithdrawTypeId(int withdrawTypeId) {
		this.withdrawTypeId = withdrawTypeId;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}


	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public long getReferUuid() {
		return referUuid;
	}

	public void setReferUuid(long referUuid) {
		this.referUuid = referUuid;
	}

	public String getWithdrawMode() {
		return withdrawMode;
	}

	public void setWithdrawMode(String withdrawMode) {
		this.withdrawMode = withdrawMode;
	}
}
