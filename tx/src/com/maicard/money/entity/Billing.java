package com.maicard.money.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import com.maicard.utils.StringTools;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class Billing extends BaseEntity {

	private static final long serialVersionUID = -1840337619819376913L;
	 
	/**
	 * 结算开始时间
	 */
	private Date billingBeginTime;
	/**
	 * 结算结束时间
	 */
	private Date billingEndTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 结算处理时间
	 */
	private Date billingHandlerTime;
	/**
	 * 结算账户UUID-商户号
	 */
	private long uuid;
	/**
	 * 结算面额
	 */
	private float faceMoney;
	/**
	 *佣金，手续费
	 */
	private float commission;

	/**
	 * 该结算单对应的分成配置
	 */
	private long shareConfigId;
	
	/**
	 * 用于唯一确定某个时间段内的结算单，防止重复计算
	 */
	private String billingKey;
	
	
	/**
	 * 应结算金额
	 */
	private float realMoney;
	
	/**
	 * 实际到账金额
	 */
	private float arriveMoney;	
	
	/**
	 * 该结算周期的期初余额，就是结算前，该账户中应该有的未结算资金
	 */
	private float beginBalance;
	
	public float getBeginBalance() {
		return beginBalance;
	}

	public void setBeginBalance(float beginBalance) {
		this.beginBalance = beginBalance;
	}

	private long objectId;		//结算对应的业务ID

	
	
	/**
	 * 谁操作
	 */
	private long operator;
	/**
	 * 是否清算处理
	 */
	private boolean isClearDeal = Boolean.TRUE;
	/**
	 * 结算方式
	 */
	private String clearWay;
	/**
	 * 清算状态
	 */
	private String clearStatus;

	private String objectType;

	/**
	 * 结算状态
	 */
	private String stateStatus;
	/**
	 * 清算类型
	 */
	private String clearType;

	public Billing(){}

	public Billing(long ownerId) {
		this.ownerId = ownerId;
		this.createTime = new Date();
	}
	
	@Override
	public String toString() {
		return new StringBuffer().append(getClass().getName()).append('@').append(Integer.toHexString(hashCode())).append('(').append("billingId=").append(id).append(",billingBeginTime=").append(StringTools.time2String(billingBeginTime)).append("=>").append(StringTools.time2String(billingEndTime)).append(",uuid=").append(uuid).append(",faceMoney=").append(faceMoney).append(",realMoney").append(realMoney).append(",currentStatus=").append(currentStatus).append(")").toString();
	}

	@Override
	public Billing clone() {
		try{
			return (Billing)super.clone();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	


	
}
