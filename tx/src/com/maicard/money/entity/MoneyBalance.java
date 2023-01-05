package com.maicard.money.entity;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
/**
 * 收支平衡计算<br>
 * 毛利 = 应收资金 - 待结算资金 - 待提现资金
 *
 *
 * @author NetSnake
 * @date 2017-11-04
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneyBalance  extends BaseEntity{



	/**
	 * 
	 */
	private static final long serialVersionUID = -6756071246040148932L;

	private int moneyBalanceId;
	
	/**
	 * 如果是针对用户，这里是跟用户关联的UUID
	 */
	private long uuid;
	
	/**
	 * 计算时间
	 */
	private Date createTime;

	/**
	 * 待结算资金，客户还不能使用，T+1客户即它当天的收入资金incomingMoney
	 */
	private double waitingSettlementMoney;

	/**
	 * 待提现资金，客户可以提现尚未提现
	 */
	private double waitingWithdrawMoney;
	
	/**
	 * 应收入资金
	 */
	private double receivableMoney;
	
	/**
	 * 实际收入资金
	 */
	private double arrivedMoney;
	
	/**
	 * 计算时间点的系统方实际余额
	 */
	private double currentBalance;
	
	/**
	 * 计算毛利
	 */
	private float profit;
	
	@Override
	public MoneyBalance clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (MoneyBalance)in.readObject();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	

	
	public MoneyBalance() {
	}
	public MoneyBalance(long ownerId) {
		this.ownerId = ownerId;
	}
	public int getMoneyBalanceId() {
		return moneyBalanceId;
	}
	public void setMoneyBalanceId(int moneyBalanceId) {
		this.moneyBalanceId = moneyBalanceId;
	}
	public double getWaitingSettlementMoney() {
		return waitingSettlementMoney;
	}
	public void setWaitingSettlementMoney(double waitingSettlementMoney) {
		this.waitingSettlementMoney = waitingSettlementMoney;
	}
	public double getWaitingWithdrawMoney() {
		return waitingWithdrawMoney;
	}
	public void setWaitingWithdrawMoney(double waitingWithdrawMoney) {
		this.waitingWithdrawMoney = waitingWithdrawMoney;
	}
	public double getReceivableMoney() {
		return receivableMoney;
	}
	public void setReceivableMoney(double receivableMoney) {
		this.receivableMoney = receivableMoney;
	}
	public float getProfit() {
		return profit;
	}
	public void setProfit(float profit) {
		this.profit = profit;
	}
	public long getUuid() {
		return uuid;
	}
	public void setUuid(long uuid) {
		this.uuid = uuid;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public double getArrivedMoney() {
		return arrivedMoney;
	}
	public void setArrivedMoney(double arrivedMoney) {
		this.arrivedMoney = arrivedMoney;
	}
	public double getCurrentBalance() {
		return currentBalance;
	}
	public void setCurrentBalance(double currentBalance) {
		this.currentBalance = currentBalance;
	}

	

	
}
