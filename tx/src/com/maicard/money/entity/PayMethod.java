package com.maicard.money.entity;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.entity.BaseEntity;
import com.maicard.views.JsonFilterView;

public class PayMethod extends BaseEntity implements Cloneable {


	private static final long serialVersionUID = -7271651617034782741L;


	private long payChannelId;

	private String name;

	private String validTimeRange;

	private int flag;

    @JsonView({JsonFilterView.Full.class})
	private String processClass;
	
	private int payTypeId;
	
	/**
	 * 该支付通道对应的虚拟系统用户UUID，每次提现将从该虚拟账户中扣除资金
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
	
	private int weight;			//使用权重，越大越优先
	
	private int percent;		//相同条件下，使用该支付方式的比例, 最大为100
	
    @JsonView({JsonFilterView.Full.class})
	private Map<String,String> data;
		
	
	private String contextType;	//应用程序的场景，@see com.maicard.standard.ContextType


	protected  String currency;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	@Override
	public PayMethod clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (PayMethod)in.readObject();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public PayMethod() {
	}

	public long getPayChannelId() {
		return payChannelId;
	}

	public void setPayChannelId(long payChannelId) {
		this.payChannelId = payChannelId;
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
		final PayMethod other = (PayMethod) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"payMethodId=" + "'" + id + "'" + 
			"name=" + "'" + name + "'" + 
			"payTypeId=" + "'" + payTypeId + "'" + 
			"processClass=" + "'" + processClass + "'" + 
			"weight=" + "'" + weight + "'" + 
			"percent=" + "'" + percent + "'" +
			"payChannelId=" + "'" + payChannelId + "'" + 
			"ownerId=" + "'" + ownerId + "'" + 
			")";
	}
	
	

	public String getProcessClass() {
		return processClass;
	}

	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	public int getPayTypeId() {
		return payTypeId;
	}

	public void setPayTypeId(int payTypeId) {
		this.payTypeId = payTypeId;
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

	public long getReferUuid() {
		return referUuid;
	}

	public void setReferUuid(long referUuid) {
		this.referUuid = referUuid;
	}
	
}
