package com.maicard.tx.entity;
 
import java.io.Serial;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maicard.core.entity.ExtraData;
import com.maicard.money.entity.Price;
import com.maicard.money.entity.TxBaseEntity;
import com.maicard.serializer.ExtraDataMapSerializer;
import com.maicard.utils.NumericUtils;
import com.maicard.views.JsonFilterView;


@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Item extends TxBaseEntity{

	
	
	//public static final String TABLE_POLICY = "MM";
	//public static final String TABLE_PREFIX = "item";

	//public static final String SEQUENCE_PREFIX = "ITEM_SEQ";

	@Serial
	private static final long serialVersionUID = -1L;

	
	private int transactionTypeId = 0;
	private String name;
	private String content;
	private long productId = 0;
	private long chargeFromAccount = 0;//从哪个帐号扣钱
	private long chargeToAccount = 0; //充值到哪个帐号


	/**
	 * 对应对象的类型ID
	 * @ignore
	 */
	private long objectTypeId;

	private String objectType;
	/**
	 * @ignore
	 */
	private float rate = 0f;

	/**
	 * 商品数量
	 */
	private float count = 0;

	/**
	 * @ignore
	 */
	private float labelMoney = 0f; //当前订单的总金额

	/**
	 * 商品金额
	 */
	private float requestMoney = 0f;  //当前需要处理的金额

	/**
	 * @ignore
	 */
	private float successMoney = 0f; //当前商品实际已完成的金额

	/**
	 * @ignore
	 */
	private float frozenMoney = 0f;	//冻结金额

	/**
	 * @ignore
	 */
	private float inMoney	= 0f;	//购入成本

	/**
	 * @ignore
	 */
	private float outMoney = 0f;  //售出成本

	/**
	 * @ignore
	 */
	private Date enterTime;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int maxRetry;	//处理商品时的最多重复次数

	/**
	 * @ignore
	 */
	private Date closeTime;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int extraStatus = 0;

	/**
	 * @ignore
	 */
	private int billingStatus = 0;

	/**
	 * @ignore
	 */
	private int outStatus = 0;

	/**
	 * @ignore
	 */
	private long cartId = 0; 					//购物车编号，与第一个商品item_id一致


	/**
	 * 该交易对应的价格
	 * @ignore
	 */
	//@JsonView({JsonFilterView.Partner.class})
	private Price price;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int processCount;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private long supplyPartnerId;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int weight;	//权重，数字越大处理优先级越高

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private long shareConfigId;

	/**
	 * @ignore
	 */
	@JsonSerialize(using = ExtraDataMapSerializer.class) 
 	private Map<String, ExtraData> itemData;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int lockStatus;
	/**
	 * @ignore
	 */
	private String chargeFromAccountName;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int beforeLockStatus;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private int afterLockStatus;

	/**
	 * @ignore
	 */
	@JsonView({JsonFilterView.Partner.class})
	private long lastEffect;		//最后一次影响业务操作的时间

 

	public Item(long ownerId) {
		this.ownerId = ownerId;
	}   


	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	public void setContent(String content) {
		this.content = content == null ? null : content.trim();
	}




	@Override
	public Item clone() {
		return (Item)super.clone();
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append('@');
		sb.append(Integer.toHexString(hashCode()));
		sb.append('(');
		sb.append("transactionId=");
		sb.append(transactionId);
		sb.append(",count=");
		sb.append(count);
		sb.append(",transactionTypeId=");
		sb.append(transactionTypeId);
		sb.append(",supplyPartnerId=");
		sb.append(supplyPartnerId);
		sb.append(",account=");
		sb.append(chargeFromAccount);
		sb.append("=>");
		sb.append(chargeToAccount);
		sb.append(",productId=");
		sb.append(productId);
		sb.append(",labelMoney=");
		sb.append(labelMoney);
		sb.append(",requestMoney=");
		sb.append(requestMoney);
		sb.append(",frozenMoney=");
		sb.append(frozenMoney);
		sb.append(",successMoney=");
		sb.append(successMoney);
		sb.append(",status=");
		sb.append(currentStatus);
		sb.append('/');
		sb.append(extraStatus);
		sb.append('/');
		sb.append(outStatus);
		sb.append(')');
		return sb.toString();
	}




}
