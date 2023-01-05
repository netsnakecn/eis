package com.maicard.money.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.views.JsonFilterView;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NeedJmsDataSyncP2P
@Data
public class Withdraw extends TxBaseEntity{

		
	
	private long withdrawTypeId;
	
	private long withdrawMethodId;
	
	
	
	/**
	 * 收款账户类型--默认银行卡
	 * bankCard 银行卡
	 * aliPayLoginId
	 * aliPayUserId
	 */
	private String payeeType;
	/**
	 * 请求提现时对应的提现方式的实例，仅用于在后续处理时临时存放该提现方式的一些参数配置，不保存
	 */
    @JsonView({JsonFilterView.Partner.class})
    private WithdrawMethod withdrawMethod;
    
	private long uuid;
	private long lockByUuid;

	private long bankAccountId;
	private BankAccount bankAccount;

	//从哪个卡发起转账
	private long fromBankAccountId;

	//没有存入数据库
	private BankAccount fromBankAccount;
	
	/**
	 * 申请提现的金额
	 */
	private float withdrawMoney;		
	
	/**
	 * 提交到银行的金额
	 */
	private float arriveMoney;
	
	/**
	 * 实际成功金额
	 */
	private float successMoney;		
	
	private float moneyBeforeWithdraw;	//提现钱的收入资金	
	private float moneyAfterWithdraw;	//提现后剩余收入资金
	private Date beginTime;

	private Date processTime;

	private int ttl;

	public void setStartTime(Date t){
		this.beginTime = t;
		this.startTime = t;
	}

	public Date getStartTime(){
		return this.beginTime;
	}
	private String commissionType;	//提现手续费类型
	private String commissionChargeType;	//提现手续费从哪里收取
	
	private int billingId;
	
	/**
	 * 是否为批付模式，是则会把批付数据转化为多个子订单
	 */
	private boolean batchMode;
	
	/**
	 * 批付时对应的总订单
	 */
	private String parentTransactionId;
	
	/**
	 * 批付的帐号个数
	 */
	private int totalRequest;
	
	/**
	 * 批付的成功帐号数量
	 */
	private int successRequest;
	
	
	/**
	 * 批付的失败数量
	 */
	private int failRequest;
	
	/**
	 * 付款理由
	 */
	private String reason;
	
	
	/**
	 * 交易版本
	 */
	private String tradeVersion;
	/**
	 * 人工付款操作类型
	 */
	private String manualType;
	/**
	 * 当前渠道请求号
	 */
	public String curChannelRequestNo;
	/**
	 * 出款会员id
	 */
	public String memberNo;
	/**
	 * 渠道状态
	 */
	public String channelStatus;
	/**
	 * 交易来源
	 */
	private String tradeSource;

	private String withdrawMode;




	public float getRealMoney(){
		return this.arriveMoney;
	}

	public void setRealMoney(float m){
		this.arriveMoney = m;
		this.realMoney = m;
	}
	
	
	/**
	 * 对应的业务代码，比如可能是对应支付方式的payTypeId
	 */
	private String businessCode;


	public Withdraw(){
	}

	public Withdraw(long ownerId){
		this.ownerId = ownerId;
	}
	

	@Override
	public Withdraw clone() {
		return (Withdraw)super.clone();
	}

}
