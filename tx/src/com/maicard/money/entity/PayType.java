package com.maicard.money.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.maicard.core.constants.CacheNames;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;

/**
 * 支付类型
 * 比如是微信支付、支付宝、网银还是点卡
 * 每种支付方式，由PayMethod确定交给哪个处理器做处理
 *
 *
 * @author NetSnake
 * @date 2016年10月20日
 *
 */

@Data
public class PayType extends BaseEntity {

	private static final long serialVersionUID = 6265390330111437054L;

	public static final String CACHE_NAME = CacheNames.cacheNameProduct;

	private String name;

	private String description;

	private int flag;
	
	private int weight;
	
	private String logoUrl;
	
	private String validAmount;

	private String cardSerialnumberLength;

	private String cardPasswordLength;
	
	private float publicRate; //该支付方式的统一费率
	
	private String inputType; //输入方式，account:账户支付, bank:银行, card:卡密
	protected  String currency;


	public PayType() {
	}
	
	@Override
	public PayType clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (PayType)in.readObject();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)id;

		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PayType other = (PayType) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"payTypeId=" + "'" + id + "'" + 
			")";
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getValidAmount() {
		return validAmount;
	}

	public void setValidAmount(String validAmount) {
		this.validAmount = validAmount;
	}

	public String getCardSerialnumberLength() {
		return cardSerialnumberLength;
	}

	public void setCardSerialnumberLength(String cardSerialnumberLength) {
		this.cardSerialnumberLength = cardSerialnumberLength;
	}

	public String getCardPasswordLength() {
		return cardPasswordLength;
	}

	public void setCardPasswordLength(String cardPasswordLength) {
		this.cardPasswordLength = cardPasswordLength;
	}

	public float getPublicRate() {
		return publicRate;
	}

	public void setPublicRate(float publicRate) {
		this.publicRate = publicRate;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
