package com.maicard.money.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.views.JsonFilterView;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
//@JsonIgnoreProperties({"payTypeId","payMethodId","payToAccount","payFromAccount","moneyTypeId","cardPassword"})

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Pay extends TxBaseEntity{


	/**
	 * 支付订单的分区模式
	 */
	public static final String TABLE_POLICY = "MM";


	private long payTypeId;

	private long payMethodId;
	
	private String payCardType;
	
	
	 /**
     * 交易类型 退款 或者支付
     */
    private String tradeType;
	
	/**
	 * 请求支付时对应的支付方式的实例，仅用于在后续处理时临时存放该支付方式的一些配置，不保存
	 */
    @JsonView({JsonFilterView.Partner.class})
	private PayMethod payMethod;
	
	private String name;
	
	private String description;

	private long payToAccount;
	
	private int payToAccountType;

	private long payFromAccount;
	
	private int payFromAccountType;

	private int moneyTypeId;

	private String refBuyTransactionId;

	private String parentPayOrderId;
	
	@JsonIgnore
	public int beforeLockStatus;

	private int flag;

	private String cardSerialNumber;

	private String cardPassword;
	
    @JsonView({JsonFilterView.Partner.class})
    private int billingId;

    @JsonView({JsonFilterView.Partner.class})
	private float rate;
	
    @JsonView({JsonFilterView.Partner.class})
	private String notifyUrl;
	
    @JsonView({JsonFilterView.Partner.class})
	private String returnUrl;
	
	private String inJumpUrl;	//向支付发起方跳转的URL






	
	private String payResultMessage; //用于给用户或接口返回的数据
	
	private Object parameter;		//用户端可能提交的参数
	
	
	private boolean createOrder = true;		//是否创建支付订单
	
	private String contextType;			//应用环境，微信、手机浏览器、PC或应用程序，@see ContextType
	
	
	
	/**
	 * 付款后账户余额，同时也用来判断一个订单是否已经被结算过，每个订单不能多次结算
	 */
	private double balance;
	
	public Pay() {
		this.startTime = new Date();
	}
	public Pay(long ownerId) {
		this.ownerId = ownerId;
		this.startTime = new Date();
	}

	


	@Override
	public Pay clone() {
		return (Pay)super.clone();
	}

	public long getPayTypeId() {
		return payTypeId;
	}

	public int getBeforeLockStatus() {
		return beforeLockStatus;
	}
	public void setBeforeLockStatus(int beforeLockStatus) {
		this.beforeLockStatus = beforeLockStatus;
	} 
	
}
