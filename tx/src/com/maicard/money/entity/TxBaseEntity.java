package com.maicard.money.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.views.JsonFilterView;
import lombok.Data;

import java.util.Date;

@Data
public abstract class TxBaseEntity extends IndexableEntity {

	private static final long serialVersionUID = -5344626724547153845L;

	/**
	 * 订单号
	 *
	 */
	protected String transactionId;

	/**
	 * 扩展状态
	 * @ignore
	 */
	protected int extraStatus;

	/**
	 * @ignore
	 */
	protected String tableName;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	protected int ttl;


	@JsonView({JsonFilterView.Partner.class})
	protected String inNotifyUrl;
	
	protected String inOrderId;
	/**
	 * @ignore
	 */
    @JsonView({JsonFilterView.Partner.class})
    protected String outOrderId;
	/**
	 * @ignore
	 */
   protected long inviter;		//邀请者，或者是属于该订单属于哪个合作伙伴

	/**
	 * @ignore
	 */
    @JsonView({JsonFilterView.Partner.class})
	protected String lockId;
    
    protected long fromAccount;
    
    protected long toAccount;
    
    protected int fromAccountType;
    
    protected int toAccountType;
	/**
	 * @ignore
	 */
	protected Date startTime;
	/**
	 * @ignore
	 */
	protected Date endTime;
    
    /**
	 * 该笔付款扣除的手续费
	 * @ignore
	 */
	protected float commission;

	/**
	 * @ignore
	 */
	@JsonIgnore
	protected int lockStatus; //锁定更新时，指定更新前的状态，如果不更新前不是这个状态，则更新失败


	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId == null ? null : transactionId.trim();
	}

	/**
	 * @ignore
	 */
	protected  String currency;

	/**
	 * 商品金额
	 */
	protected  float faceMoney;

	/**
	 * @ignore
	 */
	protected  float realMoney;


}
