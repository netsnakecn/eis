package com.maicard.money.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;

/**
 * 分成配置信息
 */

@Data
public class ShareConfig extends BaseEntity implements Cloneable{

	private static final long serialVersionUID = 1L;
	
	public static final String PLUS = "plus";
	public static final String MINUS = "minus";

	public static final int MONEY_SHARE_MODE_TO_USER	= 1;	//直接分成给当前支付用户
	
	public static final int MONEY_SHARE_MODE_TO_CHANNEL = 2;	//支付分成给用户的渠道
	/**
	 * 分成配置ID
	 */

	private String shareConfigCode;

	private String shareConfigName;


	private int objectId;

	private String objectType;
	/**
	 * 分成比例
	 */
	private float sharePercent;
	/**
	 * 资金方向，plus:增加资金，minus:减少资金
	 */
	private String moneyDirect;

	private String chargeType;
	
	private Float beginMoney;
	
	private Float endMoney;

	private long shareUuid;
	/**
	 * 分成类型，business:厂商分成, channel:推广渠道
	 */
	private String shareType;
	
	/**
	 * 付费卡类型
	 */
	private String payCardType;
	/**
	 * 分成类型对应的TTL
	 */
	private int ttl;
	/**
	 * 最大重试次数
	 */
	private int maxRetry;
	
	
	private int weight;
	/**
	 * 是否为某一产品-某一partner的默认配置
	 */
	private boolean defaultConfig;
	
	/*
	 * 交易的进入时间策略，由英文逗号分割。第一位是进入时间调整的天数，第二位是进入时间调整的秒数或固定时分秒
	 * 0,0 表示不做调整，使用当前时间
	 * 1,0 表示将进入时间向后推一天，时分秒不变
	 * 1, 02:00:01 表示将进入时间推后一天，并把时分秒设置为02:00:01，即第二天的02:00:01
	 * 1, 50 表示将进入时间退后一天，并在当前时间的基础上加50秒
	 */
	private String enterTimePolicy;	

	/**
	 * 非持久化属性/动态属性
	 */
	private String objectIdName;
	/**
	 * 结算方式 auto 自动，manual 手动
	 * 根据结算方式：将商户账户资金做结算
	 */
	private String clearWay;
	/**
	 * 清算类型
	 */
	private String clearType;
	/**
	 * 清算周期开始 00:00:00
	 */
	private String clearStartDate;
	/**
	 * 清算周期 单位时间：秒
	 * 根据开始时间做偏移
	 */
	private int offsetClearTimes;
	/**
	 * 结算账户
	 * ACCT 可用余额
	 * BANK 先到余额再到银行卡
	 */
	private String statementAccount;
	 







	public void setShareConfigCode(String shareConfigCode) {
		if(shareConfigCode != null && !shareConfigCode.trim().equals("")){
			this.shareConfigCode = shareConfigCode.trim();
		}
	}




	public void setShareConfigName(String shareConfigName) {
		if(shareConfigName != null && !shareConfigName.trim().equals("")){
			this.shareConfigName = shareConfigName.trim();
		}
	}






	@Override
	public ShareConfig clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (ShareConfig)in.readObject();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}







	@Override
	public String toString() {
		return "ShareConfig{" +
				"shareConfigId=" + id +
				", shareConfigCode='" + shareConfigCode + '\'' +
				", shareConfigName='" + shareConfigName + '\'' +
				", objectId=" + objectId +
				", sharePercent=" + sharePercent +
				", moneyDirect='" + moneyDirect + '\'' +
				", chargeType='" + chargeType + '\'' +
				", beginMoney=" + beginMoney +
				", endMoney=" + endMoney +
				", shareUuid=" + shareUuid +
				", shareType='" + shareType + '\'' +
				", payCardType='" + payCardType + '\'' +
				", ttl=" + ttl +
				", maxRetry=" + maxRetry +
				", data=" + data +
				", weight=" + weight +
				", defaultConfig=" + defaultConfig +
				", enterTimePolicy='" + enterTimePolicy + '\'' +
				", objectIdName='" + objectIdName + '\'' +
				", clearWay='" + clearWay + '\'' +
				", clearType='" + clearType + '\'' +
				", clearStartDate='" + clearStartDate + '\'' +
				", offsetClearTimes=" + offsetClearTimes +
				", statementAccount=" + statementAccount +
				'}';
	}
}
