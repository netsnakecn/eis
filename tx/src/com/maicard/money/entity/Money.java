package com.maicard.money.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import com.maicard.utils.NumericUtils;

/**
 * 资金实体类
 * XXX 重要！资金中的所有属性都以分的形式存放，在getter和setter时进行转换
 *
 *
 * @author GHOST
 * @date 2018-01-10
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Money extends BaseEntity implements Cloneable{


    private static final long serialVersionUID = -7364311150302297355L;

    public static final String MONEY_INSTANCE_PREFIX = "MONEY_INSTANCE";

    public static final String MONEY_LOCK_PREFIX = "MONEY_LOCK";

    private long uuid;

    private long marginMoney;		//定金，电商系统中使用优惠券失败（如核销成功但购买过程失败）需要转入的资金


    /**
     * 交易过程中临时锁定的资金
     */
    private long frozenMoney;

    /**
     * 在途资金或可提现资金，还未达到用户incomingMoney的，目前用于标记产生交易时，用户当时的账户余额即chargeMoney，而chargeMoney特指当时用户使用网银发起的金额
     */
    private long transitMoney;

    private long chargeMoney;			//用户自行充值得到的资金

    private long incomingMoney;	//用户收入资金，如其他账户转入，或销售某个物品后得到

    private long giftMoney;			//使用充值券等得到的资金

    private long coin;					//第三方资金，如比特币，或网站系统自身的金币

    /**
     * 被冻结coin
     */
    private long frozenCoin;

    protected  String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private long point;			//第三方资金，如比特币，或网站系统自身的点数

    /**
     * 被冻结point
     */
    private long frozenPoint;

    private long score;				//第三方资金，比如比特币，或网站系统自身的积分

    /**
     * 被冻结score
     */
    private long frozenScore;

    private String memo;

    private Long operater;

    private String accountType;

    public Money() {
    }

    public Money(long ownerId) {
        this.ownerId = ownerId;
    }

    public Money(long uuid,long ownerId) {
        this.uuid = uuid;
        this.ownerId = ownerId;
    }


    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public float getMarginMoney() {
        return toDollar(marginMoney);
    }

    public void setMarginMoney(float marginMoney) {
        this.marginMoney = toCent(marginMoney);
    }

    public float getFrozenMoney() {
        return toDollar(frozenMoney);
    }

    public void setFrozenMoney(float frozenMoney) {
        this.frozenMoney = toCent(frozenMoney);
    }

    public float getTransitMoney() {
        return toDollar(transitMoney);
    }

    public void setTransitMoney(float transitMoney) {
        this.transitMoney = toCent(transitMoney);
    }

    public float getChargeMoney() {
        return toDollar(chargeMoney);
    }

    public void setChargeMoney(float chargeMoney) {
        this.chargeMoney = toCent(chargeMoney);
    }

    public float getIncomingMoney() {
        return toDollar(incomingMoney);
    }

    public void setIncomingMoney(float incomingMoney) {
        this.incomingMoney = toCent(incomingMoney);
    }

    public float getCoin() {
        return toDollar(coin);
    }

    public void setCoin(float coin) {
        this.coin = toCent(coin);
    }

    public float getFrozenCoin() {
        return toDollar(frozenCoin);
    }

    public void setFrozenCoin(float frozenCoin) {
        this.frozenCoin = toCent(frozenCoin);
    }

    public float getPoint() {
        return toDollar(point);
    }

    public void setPoint(float point) {
        this.point = toCent(point);
    }

    public float getFrozenPoint() {
        return toDollar(frozenPoint);
    }

    public void setFrozenPoint(float frozenPoint) {
        this.frozenPoint = toCent(frozenPoint);
    }




    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Money other = (Money) obj;
        if (uuid != other.uuid)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return new StringBuffer().append(getClass().getName()).append("@").append(Integer.toHexString(hashCode()))
                .append("CENT_MODE(")
                .append("uuid=").append(uuid)
                .append(",chargeMoney=").append(chargeMoney)
                .append(",incomingMoney=").append(incomingMoney)
                .append(",giftMoney=").append(giftMoney)
                .append(",transitMoney=").append(transitMoney)
                .append(",marginMoney=").append(marginMoney)
                .append(",frozenMoney=").append(frozenMoney)
                .append(",coin=").append(coin)
                .append(",point=").append(point)
                .append(",score=").append(score)
                .append(")").toString();
    }


    public float getGiftMoney() {
        return toDollar(giftMoney);
    }

    public void setGiftMoney(float giftMoney) {
        this.giftMoney = toCent(giftMoney);
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Long getOperater() {
        return operater;
    }

    public void setOperater(Long operater) {
        this.operater = operater;
    }
    @Override
    public Money clone() {
        try{
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(this);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in =new ObjectInputStream(byteIn);

            return (Money)in.readObject();

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @JsonIgnore
    public boolean isAllZero(){
        if(this.chargeMoney + this.incomingMoney +  this.giftMoney + this.coin + this.point + this.score > 0){
            return false;
        }
        return true;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getFrozenScore() {
        return frozenScore;
    }

    public void setFrozenScore(long frozenScore) {
        this.frozenScore = frozenScore;
    }


    @JsonIgnore
    public String getMoneyBrief() {
        StringBuilder sb = new StringBuilder();
        sb.append("CENT MODE:");
        sb.append(this.chargeMoney <= 0 ? "" : "chargeMoney=" + this.chargeMoney + ',').append( this.transitMoney <= 0 ? "" : "transitMoney=" + this.transitMoney + ',').append( this.incomingMoney <= 0 ? "" : "incomingMoney=" + this.incomingMoney + ',').append( this.giftMoney <= 0 ? "" : "giftMoney=" + this.giftMoney + ',').append(this.coin <= 0 ? "" : "coin=" + this.coin + ',').append(this.point <= 0 ? "" : "point=" + this.point + ',').append(this.score <= 0 ? "" : "score=" + this.score);
        return sb.toString().replaceAll(",$", "");
    }

    /**
     * 从元转换为分
     *
     *
     * @author GHOST
     * @date 2018-01-10
     */
    public static long toCent(float value){
        return Math.round(NumericUtils.round(value * 100, 0, BigDecimal.ROUND_DOWN));
    }

    /**
     * 从分转换为元
     *
     *
     * @author GHOST
     * @date 2018-01-10
     */

    public static float toDollar(long centValue){
        return (float)NumericUtils.round((float)centValue / 100);

    }

    /**
     * 计算两个Money之间的差值
     *
     *
     * @author GHOST
     * @date 2018-11-19
     */
    public static Money remain(Money money1, Money money2) {
        Money money = new Money(money1.getOwnerId());
        float chargeMoney = money1.getChargeMoney() - money2.getChargeMoney();
        if(chargeMoney > 0) {
            money.setChargeMoney(chargeMoney);
        }
        float coin = money1.getCoin() - money2.getCoin();
        if(coin > 0) {
            money.setCoin(coin);
        }
        float point = money1.getPoint() - money2.getPoint();
        if(point > 0) {
            money.setPoint(point);
        }

        long score = money1.getScore() - money2.getScore();
        if(score > 0) {
            money.setScore(score);
        }
        return money;
    }

    /**
     *
     *
     *
     * @author GHOST
     * @date 2018-11-19
     */
    public static Money from(Price price, long uuid) {
        Money money = new Money(uuid, price.getOwnerId());
        if(price.getMoney() > 0) {
            money.setChargeMoney(price.getMoney());
        }
        if(price.getCoin() > 0) {
            money.setCoin(price.getCoin());
        }
        if(price.getScore() > 0) {
            money.setScore(price.getScore());
        }
        if(price.getPoint() > 0) {
            money.setPoint(price.getPoint());
        }
        return money;
    }


}
