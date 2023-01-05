package com.maicard.money.entity;


import com.maicard.core.entity.BaseEntity;
/**
 * 支付通道的配置数据
 *
 *
 * @author NetSnake
 * @date 2016年10月16日
 *
 */
public class PayChannelMechInfo  extends BaseEntity{

	private static final long serialVersionUID = 3601745137856541198L;

	public String accountId;
	public String accountName;
	public String payKey;
	public String cryptKey;
	public boolean forceSetPayDescription;
	public String payDescription;
	public String submitUrl;
	public String queryUrl;
	
	public float exchangeRate;
	

	/**
	 * 提交失败时是否返回处理中
	 */	
	public String setInProgressWhenSubmitFail;
	
	
	/**
	 * 提现校验KEY
	 */
	public String withdrawKey;
	
	/**
	 * 提现加密KEY
	 */
	public String withdrawCryptKey;
	
	
	/**
	 * 小额提现地址
	 */
	public String smallWithdrawUrl;
	
	/**
	 * 大额提现地址
	 */
	public String bigWithdrawUrl;
	
	/**
	 * 提现查询地址
	 */
	public String withdrawQueryUrl;
	
	public long ownerId;
	public int useCount;
	public String peerPublicKey;
	
	/**
	 * 银行账号相关信息
	 */
	public String bankName;
	public String bankAccountName;
	public String bankAccountNo;
	public String province;
	public String city;
	public String issueBank;
	
	@Override
	public String toString() {
		return new StringBuffer().append(getClass().getName()).append('@').append(Integer.toHexString(hashCode())).append('(').append("accountId=").append("'").append(accountId).append("',").append("accountName=").append("'").append(accountName).append("',").append("payKey=").append("'").append(payKey).append("')").toString();
	}


}
