package com.maicard.money.util;

import com.maicard.core.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.money.constants.MoneyType;
import com.maicard.money.entity.Money;

import java.math.BigDecimal;

public class MoneyUtils {

	protected static final Logger logger = LoggerFactory.getLogger(MoneyUtils.class);

	/**
	 * 解析以#+;分割的多个Money字段需求
	 * 例如 chargeMoney#10;coin#20;point#50 解析为资金
	 * @param priceString
	 * @return
	 */
	public static Money parseMoney(String priceString) {
		Money money = new Money();
		if(StringUtils.isBlank(priceString)){
			logger.debug("尝试解析为资金的字符串[" + priceString + "]为空");
			return money;
		}
		String data1[] = priceString.split(";");
		for(String data : data1){
			String data2[] = data.split("#");
			if(data2.length < 2){
				continue;
			}
			String moneyType = data2[0];
			String moneyValue = data2[1];
			if(moneyType.equalsIgnoreCase(MoneyType.money.getCode())){
				float value = Float.parseFloat(moneyValue);
				money.setChargeMoney(value);
			} else if(moneyType.equalsIgnoreCase(MoneyType.coin.getCode())){
				float value = Float.parseFloat(moneyValue);
				money.setCoin(value);
			} else if(moneyType.equalsIgnoreCase(MoneyType.point.getCode())){
				float value = Float.parseFloat(moneyValue);
				money.setPoint(value);
			} else if(moneyType.equalsIgnoreCase(MoneyType.score.getCode())){
				int value = Integer.parseInt(moneyValue);
				money.setScore(value);
			}
		}
		logger.debug("将字符串[" + priceString + "]解析为资金对象:" + money);
		return money;
	}
	
	public static void main(String[] argv){
		String m = "coin#2";
		Money money = parseMoney(m);
		System.out.println(money.isAllZero());
	}

	public static Money doubling(Money baseMoney, int doubling) {
		Money money = new Money();
		if(baseMoney.getChargeMoney() > 0){
			money.setChargeMoney(baseMoney.getChargeMoney() * doubling);
		}
		if(baseMoney.getCoin() > 0){
			money.setCoin(baseMoney.getCoin() * doubling);
		}
		if(baseMoney.getPoint() > 0){
			money.setPoint(baseMoney.getPoint() * doubling);
		}
		if(baseMoney.getScore() > 0){
			money.setScore(baseMoney.getScore() * doubling);
		}
		return money;
	}
	
	public static String toUserFormat(Money money) {
		StringBuffer sb = new StringBuffer();
		sb.append("money:").append(money.getChargeMoney()).append(",coin:").append(money.getCoin()).append(",point:").append(money.getPoint()).append(",score:").append(money.getScore());
		return sb.toString();
	}

	/**
	 * 把两个资金相加并返回一个新的资金对象
	 * 
	 * 
	 * @author GHOST
	 * @date 2018-11-30
	 */
	public static Money plus(Money money1, Money money2) {
		Money money = new Money(money1.getUuid(), money1.getOwnerId());
		money.setChargeMoney(money1.getChargeMoney() + money2.getChargeMoney());
		money.setCoin(money1.getCoin() + money2.getCoin());
		money.setPoint(money1.getPoint() + money2.getPoint());
		money.setScore(money1.getScore() + money2.getScore());	
		
		return money;
	}
	
	/**
	 * 把两个资金相减并返回一个新的资金对象
	 * 
	 * 
	 * @author GHOST
	 * @date 2018-11-30
	 */
	public static Money minus(Money money1, Money money2) {
		Money money = new Money(money1.getUuid(), money1.getOwnerId());
		money.setChargeMoney(money1.getChargeMoney() - money2.getChargeMoney());
		money.setCoin(money1.getCoin() - money2.getCoin());
		money.setPoint(money1.getPoint() - money2.getPoint());
		money.setScore(money1.getScore() - money2.getScore());	
		
		return money;
	}


    public static long roundToCent(float amount) {
		BigDecimal bd = new BigDecimal(amount);
		bd = bd.setScale(Constants.MONEY_ROUND_LENGTH, Constants.MONEY_ROUND_TYPE);
		return bd.intValue() * 100;
    }
}
