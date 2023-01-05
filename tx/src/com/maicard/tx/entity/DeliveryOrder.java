package com.maicard.tx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;
import com.maicard.money.entity.Price;
import com.maicard.money.vo.DeliveryTraceVo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 配送单据
 *
 * @author NetSnake
 * @date 2015年8月25日 
 */

@NeedJmsDataSyncP2P
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeliveryOrder extends AddressBook  {

	private static final long serialVersionUID = 4799784518961017943L;

	public static final String KEY_DELIVERY_ORDER_PREFIX = "KEY_DELIVERY_ORDER";


	private String outOrderId;	//外部快递单号

	private String deliveryCompany;	//快递公司
		

	private long deliveryCompanyId;	//快递公司在我方平台ID

	private Date createTime;				//创建时间

	private Date closeTime;				//结束时间

	private String refOrderId;	//	相关联的交易订单号

	private String memory;		//备注
	
	private String fromProvince;

	private String fromArea;
	
	private String toProvince;

	private String toArea;

	private float goodsWeight;			//订单重量,以克为单位，计算时应注意转换

	private String limit;			//是否只能走陆运或空运

	private Price fee;				//配送单的费用

	private List<DeliveryTraceVo> traceData;		//跟踪数据
	
	private String displayType;			//显示配送跟踪信息的格式


	
	private String brief;		//仅用于显示整个快递单所有数据整合起来的简短信息

	


	public DeliveryOrder(){

	}

	public DeliveryOrder(long ownerId) {
		this.ownerId = ownerId;
		this.createTime = new Date();
	}
	
	public DeliveryOrder(AddressBook addressBook) {
		this.uuid = addressBook.uuid;
		this.country = addressBook.country;
		this.province = addressBook.province;
		this.city = addressBook.city;
		this.district = addressBook.district;
		this.contact = addressBook.contact;
		this.phone = addressBook.phone;
		this.mobile = addressBook.mobile;
		this.postcode = addressBook.postcode;
		this.address = addressBook.address;
		this.ownerId = addressBook.getOwnerId();
		this.createTime = new Date();
	}
	
	@Override
	public DeliveryOrder clone() {
		return (DeliveryOrder)super.clone();
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append("@");
		sb.append(Integer.toHexString(hashCode()));
		sb.append("(");
		sb.append("deliveryOrderId=");
		sb.append("'");
		sb.append(id);
		sb.append("',");
		sb.append("outOrderId=");
		sb.append("'");
		sb.append(outOrderId);
		sb.append("',");
		sb.append("deliveryCompanyId=");
		sb.append("'");
		sb.append(deliveryCompanyId);
		sb.append("',");
		sb.append("goodsWeight=");
		sb.append("'");
		sb.append(goodsWeight);
		sb.append("')");
		sb.append("contact=");
		sb.append("'");
		sb.append(contact);
		sb.append("',");
		
		return sb.toString();
	}


	public String getBrief(){
		if(this.brief != null){
			return this.brief;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(this.province == null ?  "" : this.province + " ").append(this.city == null ? "" : this.city + " ").append(this.district == null ? "" : this.district + " ").append(this.address == null ? "" : this.address + " ").append(this.contact == null ? "" : this.contact + " ").append(this.mobile == null ? "" : this.mobile);
		this.brief = sb.toString();
		return this.brief;
	}



}
