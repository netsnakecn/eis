package com.maicard.tx.entity;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.entity.BaseEntity;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.money.entity.Money;
import com.maicard.money.entity.Price;
import com.maicard.views.JsonFilterView;
import lombok.Data;

/**
 * 2016-02-27
 * 增加了对应的交易订单、价格等，成为一个包含了若干交易订单的用户订单
 * 初期为购物车，仅提供一个cartId
 * 
 *
 *
 * @author NetSnake
 * @date 2016年2月27日
 *
 */

@NeedJmsDataSyncP2P
@Data
public class Order extends BaseEntity{

	public static final String OBJECT_TYPE = Order.class.getSimpleName().toLowerCase(Locale.ROOT);

    private static final long serialVersionUID = 7019579580265081277L;
	
	@JsonIgnore
	public static final String ORDER_TYPE_TEMP = "TEMP";
	@JsonIgnore
	public static final String ORDER_TYPE_STORE = "STORE";

	public static final String ORDER_TYPE_MANAGER = "MANAGER";


	@JsonIgnore
	public static final String BUY_TYPE_NORMAL = "NORMAL";
	@JsonIgnore
	public static final String BUY_TYPE_DIRIECT = "DIRECT_BUY";
	

	private long uuid = 0;

	private String goodsDesc;

	private Date createTime;
	
	private Date endTime;
	
	private Date lastAccessTime;	//最后一次访问或操作该订单的时间
	
    @JsonView({JsonFilterView.Partner.class})
	private long ttl;
	
    @JsonView({JsonFilterView.Partner.class})
	private String priceType;				//订单的价格类型，不同价格类型不能作为同一个订单
	
    @JsonView({JsonFilterView.Partner.class})
	private String orderType  = ORDER_TYPE_TEMP;						//订单类型
    
    @JsonView({JsonFilterView.Partner.class})
	private String buyType = BUY_TYPE_NORMAL;
	
	private String[] transactionIds;		//该订单所包含的交易订单号
	
	private Price price;					//该订单的价格
	
	private float totalMoney;					//该订单的总金额

	private float paidMoney;	//已支付金额

	private String moneyType;

	private long fromAddressBookId;
	 
	private long addressBookId;
	
	private AddressBook addressBook;
	
	/**
	 * 赞助模式下的赞助类型
	 */
	private String quotaType;
	
	/**
	 * 总份数
	 */
	private long totalQuota = -1;

	private float deliveryFee;

	private String extraStatus;
	
	
	/**
	 * 已完成份数
	 */
	private long successQuota;
	
	/**
	 * 已锁定份数
	 */
	private long lockedQuota;
	
	/**
	 * 锁定时lockedQuota必须为这个数，否则认为锁定失败
	 */
    @JsonView({JsonFilterView.Partner.class})
	private long beforeLockQuota = -1;

	@JsonIgnore
	private int lockStatus;
	
	private int totalProduct;				//该订单包含的总产品数
	
	private float totalGoods;				//该订单包含的总物品数，即所有产品数X购买数量
	
    @JsonView({JsonFilterView.Partner.class})
	private Map<String,Price> feeMap;		//该订单可能的减免费用
	
	private long deliveryOrderId;			//该订单对应的快递单
	
	private String invoiceInfo;				//发票信息，可能是发票JSON内容或ID
	
	/**
	 * 用于展现给用户的各种明细数据
	 */
	private Map<String,String> specInfo;
	
    @JsonView({JsonFilterView.Partner.class})
	private String identify;

    private List<Item> itemList;			//对应的交易简单数据
    
    private long inviter;						//对应的推广用户ID

	public Order() {
		this.createTime = new Date();
		this.lastAccessTime = this.createTime;
	}

	public Order(long ownerId) {
		this.ownerId = ownerId;
		this.createTime = new Date();
		this.lastAccessTime = this.createTime;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)id;

		return result;
	}
	
	@Override
	public Order clone() {
		 return (Order)super.clone();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Order other = (Order) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"orderId=" + "'" + id + "'" + 
			")";
	}


	public void setTransactionIds(String... transactionIds) {
		this.transactionIds = transactionIds;
	}

	public List<Item> getItemList() {
		if(itemList == null){
			itemList = new ArrayList<Item>();
		}
		return itemList;
	}


	 

	

}
