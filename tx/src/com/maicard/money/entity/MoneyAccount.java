package com.maicard.money.entity;

import com.maicard.core.entity.BaseEntity;
import com.maicard.money.constants.AccountTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author: iron
 * @description: 账户信息
 * @date : created in  2017/12/21 14:40.
 */

@Data
public class MoneyAccount extends BaseEntity {

	private static final long serialVersionUID = 1203797466382495934L;
	/**
     * 账户号
     */
    private String accountNo;
    /**
     *  账户类型
     */
    private String accountType;
   
    
    private String accountCategory;
    
    /**
     * 账户状态
     */
    private String accountStatus;
    /**
     *  账户名称
     */
    private String accountName;
    /**
     * 扩展参数
     */
    private String extParams;
    /**
     * 总金额
     */
    private long totalAmount;
    /**
     * 可用金额
     */
    private long availableAmount;
    /**
     * 冻结金额
     */
    private long freezeAmount;
    /**
     * 风险金额
     */
    private long riskAmount;
    /**
     * 货币类型
     */
    private String currency;

    private Date createTime;

    private Date modifyTime;

    /**
     * 商户号
     */
    private long uuid;

    public MoneyAccount() {
    }

    public MoneyAccount(String accountNo, AccountTypeEnum accountType, long uuid, String accountStatus, String accountName) {
        this.accountNo = accountNo;
        this.accountType = accountType.code;
        this.uuid = uuid;
        this.accountStatus = accountStatus;
        this.accountName = accountName;
        this.createTime = new Date();
    }


 
	@Override
    public String toString() {
        return "AccountInfoEntity{" +
                super.toString() +
                ", accountNo='" + accountNo + '\'' +
                ", accountType='" + accountType + '\'' +
                ", uuid='" + uuid + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", accountName='" + accountName + '\'' +
                ", extParams='" + extParams + '\'' +
                ", totalAmount=" + totalAmount +
                ", availableAmount=" + availableAmount +
                ", freezeAmount=" + freezeAmount +
                ", riskAmount=" + riskAmount +
                ", currency='" + currency + '\'' +
                '}';
    }

}
