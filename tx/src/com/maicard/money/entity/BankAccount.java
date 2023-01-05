package com.maicard.money.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;

import java.util.Date;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccount extends BaseEntity {

	private static final long serialVersionUID = -4384275117625705324L;
	
	
	
	/**
	 * 对公账户
	 */
	public static final String BANK_ACCOUNT_TYPE_COMPANY = "COMPANY";
	
	/**
	 * 对私账户
	 */
	public static final String BAKC_ACCOUNT_TYPE_PERSONAL = "PERSONAL";

	private int serverId;

	private float balance;

	private String bankAccountCode;
	
	/**
	 * //对公还是对私，合法输入@see CriteriaMap
	 */
	private String bankAccountType;

	//上级UUID
	private long parentUuid;

	//业务UUID
	private long referUuid;

	private long uuid;

	//被谁锁定使用中
	private long lockByUuid;

	//账号用途
	private String useType;
	
	/**
	 * 使用该帐号的用户类型，是商户还是个人用户
	 */
	private int userTypeId;
	
	private String country;
	
	private String province;
	
	private String city;
	/**
	 * 银行编码
	 */
	private String bankCode;
	/**
	 * 银行名称
	 */
	private String bankName;

	private String subsidiaryCode;
	/**
	 * 开户行
	 */
	private String issueBank;
	/**
	 * 账号
	 */
	private String bankAccountNumber;
	/**
	 * 开户名
	 */
	private String bankAccountName;
	/**
	 * 银行卡类型
	 */
	private String bankCardType;

	/**
	 * 银行账号的登陆密码
	 */
	private String loginName;

	private String loginPassword;

	private String payPassword;

	/**
	 * 证件号
	 */
	private String encryptIdNum;
	/**
	 * 加密手机号
	 */
	private String encryptMobileNo;

	private Date createTime;

	private Date lastUseTime;
	
	private int useCount;
	
	private long withdrawMethodId;
	
	/**
	 * 认证相关的文件附件
	 */
	private String certifyFile;

	@JsonIgnore
	protected int lockStatus;
	


	@Override
	public BankAccount clone() {
		return (BankAccount)super.clone();
	}
	
	public BankAccount() {
	}



	

	@Override
	public String toString(){
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"id=" + "'" + id + "'" + 
				"uuid=" + "'" + uuid + "'" + 
				"bankAccountType=" + "'" + bankAccountType + "'" + 
				"bankName=" + "'" + bankName + "'" + 
				"issueBank=" + "'" + issueBank + "'" + 
				"bankAccountNumber=" + "'" + bankAccountNumber + "'" + 
				"bankAccountName=" + "'" + bankAccountName + "'" + 
				")";
	}



}
