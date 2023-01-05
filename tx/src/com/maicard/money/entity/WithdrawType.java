package com.maicard.money.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawType extends BaseEntity{

	private static final long serialVersionUID = -7227397669789847230L;
	
	/**
	 * 默认允许的提现周期是d1，即次日提现
	 */
	public static final String DEFAULT_WITHDRAW_ARRIVE_PERIOD = "d1";		

	
	/**
	 * 提现手续费从提现资金中扣除，提现实际到账金额 = 提现金额-手续费
	 */
	public static final String COMMISSION_CHARGE_TYPE_IN_WITHDRAW = "COMMISSION_CHARGE_TYPE_IN_WITHDRAW";		
	/**
	 * 提现手续费从用户账户余额中扣除，提现实际到账金额 = 提现金额，用户账户将减去相应手续费
	 */
	public static final String COMMISSION_CHARGE_TYPE_IN_REMAIN_MONEY = "COMMISSION_CHARGE_TYPE_IN_REMAIN_MONEY";

	/**
	 * 提现类型使用父类id
	 */
	/**
	 * 类型名称
	 */
	private String withdrawTypeName;
	/**
	 * 交易类型
	 */
	private String tradeType;
	/**
	 * 会员id
	 */
	private String memberNo;

	/**
	 * 提现手续费，根据手续费类型进行计算
	 */
	private float commission;
	/**
	 * 提现手续费类型，是固定费率还是按金额的比例
	 */
	private String commissionType;
	/**
	 * 提现手续费的收取方式，从提现金额中扣除，或从用户余额中扣除
	 */
	private String commissionChargeType;
	/**
	 * 到账周期，如T0为当日结算，M1为次月结算
	 */
	private String arrivePeriod;
	/**
	 * 允许提现的开始时间
	 */
	private Date withdrawBeginTime;
	/**
	 * 允许提现的结束时间
	 */
	private Date withdrawEndTime;
	/**
	 * 在一个提现周期内可提现次数
	 */
	private int maxWithdrawCountInPeriod;
	/**
	 * 在一个提现周期内允许的最大提现金额
	 */
	private float maxWithdrawAmountInPeriod;
	/**
	 * 每次提现的最大金额
	 */
	private float maxWithdrawAmountPerCount;
	/**
	 * 每次提现的最小金额
	 */
	private float minWithdrawAmountPerCount;

	protected  String currency;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getWithdrawTypeName() {
		return withdrawTypeName;
	}

	public void setWithdrawTypeName(String withdrawTypeName) {
		this.withdrawTypeName = withdrawTypeName;
	}

	public Date getWithdrawBeginTime() {
		return withdrawBeginTime;
	}

	public void setWithdrawBeginTime(Date withdrawBeginTime) {
		this.withdrawBeginTime = withdrawBeginTime;
	}

	public Date getWithdrawEndTime() {
		return withdrawEndTime;
	}

	public void setWithdrawEndTime(Date withdrawEndTime) {
		this.withdrawEndTime = withdrawEndTime;
	}


	public String getArrivePeriod() {
		return arrivePeriod;
	}

	public void setArrivePeriod(String arrivePeriod) {
		this.arrivePeriod = arrivePeriod;
	}

	public float getMaxWithdrawAmountInPeriod() {
		return maxWithdrawAmountInPeriod;
	}

	public void setMaxWithdrawAmountInPeriod(float maxWithdrawAmountInPeriod) {
		this.maxWithdrawAmountInPeriod = maxWithdrawAmountInPeriod;
	}

	public float getMaxWithdrawAmountPerCount() {
		return maxWithdrawAmountPerCount;
	}

	public void setMaxWithdrawAmountPerCount(float maxWithdrawAmountPerCount) {
		this.maxWithdrawAmountPerCount = maxWithdrawAmountPerCount;
	}

	public float getMinWithdrawAmountPerCount() {
		return minWithdrawAmountPerCount;
	}

	public void setMinWithdrawAmountPerCount(float minWithdrawAmountPerCount) {
		this.minWithdrawAmountPerCount = minWithdrawAmountPerCount;
	}

	public int getMaxWithdrawCountInPeriod() {
		return maxWithdrawCountInPeriod;
	}

	public void setMaxWithdrawCountInPeriod(int maxWithdrawCountInPeriod) {
		this.maxWithdrawCountInPeriod = maxWithdrawCountInPeriod;
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

	public String getCommissionChargeType() {
		return commissionChargeType;
	}

	public void setCommissionChargeType(String commissionChargeType) {
		this.commissionChargeType = commissionChargeType;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getMemberNo() {
		return memberNo;
	}

	public void setMemberNo(String memberNo) {
		this.memberNo = memberNo;
	}
}
