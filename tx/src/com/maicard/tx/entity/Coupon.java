package com.maicard.tx.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.constants.KeyConstants;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.money.constants.TxStatus;
import com.maicard.views.JsonFilterView;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@NeedJmsDataSyncP2P
@Data
public class Coupon extends CouponModel  {

    public static final String TRADE_COUPON_INCOME = "COUPON_INCOME" ;
	public static final String TRADE_COUPON_CONSUME = "COUPON_CONSUME" ;
	private static final long serialVersionUID = 976395645315534126L;

	public static final int STATUS_NEW = TxStatus.newOrder.id;
	public static final int STATUS_LOCKED = TxStatus.auctionSuccess.id;
	public static final int STATUS_USED = TxStatus.closed.id;

	@JsonView({JsonFilterView.Partner.class})
	private long id;			//PK

	private long couponModelId;


	private String transactionId;


	private String couponSerialNumber;		//优惠券序列号

	private String couponPassword;		//优惠券密码

	private long uuid;					//谁领取了该优惠券



	private Date fetchTime;

	private Date useTime;
	
		


	@JsonView({JsonFilterView.Full.class})
	private int lockStatus;	//锁定时的全局锁定ID



	public Coupon() {
	}
	public Coupon(long ownerId) {
		this.ownerId = ownerId;
	}

	public Coupon(CouponModel couponModel) {
		this.id = couponModel.id;
		this.couponType = couponModel.couponType;
		this.couponCode = couponModel.couponCode;
		this.couponModelName= couponModel.couponModelName;
		this.couponModelDesc = couponModel.couponModelDesc;
		this.content = couponModel.content;
		this.binContent = couponModel.binContent;
		this.giftMoney = couponModel.giftMoney;
		this.giftMoneyType = couponModel.giftMoneyType;
		this.giftMoney = couponModel.giftMoney;
		this.giftMoneyType = couponModel.giftMoneyType;
		this.validTimeBegin = couponModel.validTimeBegin;
		this.validTimeEnd = couponModel.validTimeEnd;
		this.imageUrl = couponModel.imageUrl;
		this.ownerId = couponModel.getOwnerId();
		this.processor = couponModel.processor;
		this.extraCode = couponModel.getExtraCode();
	}



	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"couponId=" + "'" + id + "'," +
				"couponModelId=" + "'" + couponModelId + "'," + 
				"couponSerialNumber=" + "'" + couponSerialNumber + "'," + 
				"couponPassword=" + "'" + couponPassword + "'," + 
				"currentStatus=" + "'" + currentStatus + "'," + 
				"uuid=" + "'" + uuid + "'," + 
				"ownerId=" + "'" + ownerId + "'" + 
				")";
	}

	@Override
	public Coupon clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (Coupon)in.readObject();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	
	public String getLockKey() {
		return KeyConstants.LOCKED_COUPON_PREFIX + "#" + this.id;
	}
}
