package com.maicard.money.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;

import java.util.Date;


@Data
public class MoneyFlow extends BaseEntity {

	private String accountNo;
	
	private String accountType;

    private Date createTime;
    private Date modifyTime;

    private long uuid;
    private String fromMerchantNo;
    private String fromAccountNo;
    private String tradeType;
    private String subTradeType;
    private String fundType;

    private String optType;
    private long tradeAmount;
    private long finishAmount;
    private long orderAmount;
    private String transactionId;
    private String bankOrderId;
    private String currency;
    private String payChannel;

    private long shareConfigId;
    /**
     * 卡类型
     */
    private String cardType;
    private String feeRate;
    private String accountCategory;
    private String tradeStatus;
    private String clearStatus;
    private String clearType;
    private String tradeSubject;
    private String extParams;
    
    @Override
	public MoneyFlow clone() {
			return (MoneyFlow)super.clone();
	}

    public MoneyFlow(){}

    public MoneyFlow(long ownerId){
        this.ownerId = ownerId;
        this.createTime = new Date();
    }


    @Override
    public String toString() {
        return "AccountFlowEntity{" +
                super.toString()+
                ", accountNo='" + accountNo + '\'' +
                ", memberNo='" + uuid + '\'' +
                ", payChannel='" + payChannel + '\'' +
                ", orgMemberNo='" + fromMerchantNo + '\'' +
                ", orgAccountNo='" + fromAccountNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", subTradeType='" + subTradeType + '\'' +
                ", fundType='" + fundType + '\'' +
                ", cardType='" + cardType + '\'' +
                ", feeRate='" + feeRate + '\'' +
                ", optType='" + optType + '\'' +
                ", tradeAmount=" + tradeAmount +
                ", finishAmount=" + finishAmount +
                ", orderAmount=" + orderAmount +
                ", transactionId='" + transactionId + '\'' +
                ", bankOrderId='" + bankOrderId + '\'' +
                ", currency='" + currency + '\'' +
                ", accountCategory='" + accountCategory + '\'' +
                ", tradeStatus='" + tradeStatus + '\'' +
                ", clearStatus='" + clearStatus + '\'' +
                ", clearType='" + clearType + '\'' +
                ", tradeSubject='" + tradeSubject + '\'' +
                ", extParams='" + extParams + '\'' +
                '}';
    }


	public String getAccountType() {
		if(accountType == null) {
			accountType = accountNo.substring(accountNo.length() - 4);
		}
		return accountType;
	}

}
