package com.maicard.tx.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.entity.BaseEntity;
import com.maicard.views.JsonFilterView;
import lombok.Data;

/**
 *
 *	优惠券产品对象，即静态对象
 *
 * @author NetSnake
 * @date 2015年11月25日
 * 
 */

@Data
public class CouponModel extends BaseEntity   {

	private static final long serialVersionUID = -4655346607691924514L;


	protected long id;			//PK

	@JsonView({JsonFilterView.Full.class})
	private long parentCouponModelId;			//上级产品ID

	protected String couponType;				//卡券产品类型

	protected int level;					//级别

	protected String couponCode;			//优惠券编码

	protected String extraCode;			//外部编码

	protected String couponModelName;			//优惠券名称

	protected String couponModelDesc;			//优惠券说明

	protected String content;			//优惠券的内容，比如条码等

	protected byte[] binContent;			//优惠券的二进制内容，比如图片

	@JsonView({JsonFilterView.Full.class})
	protected String promotionData;			//优惠配置

	@JsonView({JsonFilterView.Full.class})
	protected String processor;			//处理器

	protected String imageUrl;			//该优惠券的对应显示图片


	protected float giftMoney;			//需要多少钱兑换

	protected	String giftMoneyType;			//赠送多少钱

	protected  float priceMoney;

	protected  String priceMoneyType;

	/**
	 * 微信卡券中的描述
	 */
	protected String memory;			

	protected Date validTimeBegin;		//有效期;

	protected String identify;			//识别码

	protected Date validTimeEnd;		

	/**
	 * 用户对应的推广渠道、经销商
	 */
	@JsonView({JsonFilterView.Partner.class})
	private long inviter;			

	private int minKeepCount;			//该卡券在系统中的最少保有量

	private int maxKeepCount;			//该卡券在系统中的最多保有量


	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"couponModelId=" + "'" + id + "'," +
				"couponCode=" + "'" + couponCode + "'," + 
				"couponModelName=" + "'" + couponModelName + "'," + 
				"currentStatus=" + "'" + currentStatus + "'," + 
				"ownerId=" + "'" + ownerId + "'" + 
				")";
	}

	@Override
	public CouponModel clone(){
		try{
			return (CouponModel)super.clone();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}



}
