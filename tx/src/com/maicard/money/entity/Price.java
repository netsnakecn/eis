package com.maicard.money.entity;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import com.maicard.utils.NumericUtils;
import lombok.Data;

/**
 * 产品价格规则
 * 在很多时候，产品价格较复杂的时候，作为产品价格的标准
 * 
 * 
 * @author NetSnake
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Price extends BaseEntity{

	private static final long serialVersionUID = -8252816152534507121L;
	
	public static final String PRICE_STANDARD = "PRICE_STANDARD";//, //标准价格
	public static final String PRICE_TUAN = "PRICE_TUAN";//, //团购价格
	public static final String PRICE_MEMBER = "PRICE_MEMBER";//, //会员价格
	public static final String PRICE_PROMOTION = "PRICE_PROMOTION";//,	//优惠价格，可以与identify配合使用识别不同的优惠活动
	public static final String PRICE_SALE = "PRICE_SALE";//售出价格



	private float marketPrice;	//市场价

	private float money;		//如果>0，兑换时需要扣除相应的资金

	private float coin;		//兑换所需的金币

	private float point;		//兑换所需的点数
	
	private long score;		//兑换所需的积分

	private String groupName;

	private String priceName;

	private long parentId;


	private String objectType;	//兑换的物品类型，如product
	
	private long objectId;	//关联的商品
	
	private String priceType;	//价格类型，@see com.maicard.standard.PriceType
	
	private String identify;		//特殊价格的识别，比如某个具体活动需要特殊价格，格式activity#5标记为是给5#活动指定的价格
	
	private String feeAdjectInfo;			//其他减免费用的配置
	
	public Price() {
	}

	public Price(String priceType) {
		this.priceType = priceType;
	}
	
	public Price(long ownerId) {
		this.ownerId = ownerId;
	}

	public void setObjectType(String objectType) {
		if(objectType != null && !objectType.trim().equals("")){
			this.objectType = objectType.trim();
		}
	}



	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append("@");
		sb.append(Integer.toHexString(hashCode()));
		sb.append("(");
		sb.append("priceId=");
		sb.append("'");
		sb.append(id);
		sb.append("',");
		sb.append("priceType=");
		sb.append("'");
		sb.append(priceType);
		sb.append("',");
		sb.append("identify=");
		sb.append("'");
		sb.append(identify);
		sb.append("',");
		sb.append("money=");
		sb.append("'");
		sb.append(money);
		sb.append("',");
		sb.append("coin=");
		sb.append("'");
		sb.append(coin);
		sb.append("',");
		sb.append("point=");
		sb.append("'");
		sb.append(point);
		sb.append("',");
		sb.append("score=");
		sb.append("'");
		sb.append(score);
		sb.append("',");
		sb.append("object=");
		sb.append("'");
		sb.append(objectType);
		sb.append(".");
		sb.append(objectId);
		sb.append("')");
		return sb.toString();

	}
	public float getMarketPrice() {
		return marketPrice;
	}
	public void setMarketPrice(float marketPrice) {
		this.marketPrice = marketPrice;
	}
	

	@Override
	public Price clone() {
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(this);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);

			return (Price)in.readObject();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	 

	public void setIdentify(String identify) {
		if(identify == null || identify.trim().equals("")){
			return;
		}
		this.identify = identify;
	}

	public static Price add(Price price1, Price price2) {
		if(price1 == null || price2 == null){
			return null;
		}
		Price price = price1.clone();
		price.setMoney(price1.getMoney() + price2.getMoney());
		price.setCoin(price1.getCoin() + price2.getCoin());
		price.setPoint(price1.getPoint() + price2.getPoint());
		price.setScore(price1.getScore() + price2.getScore());
		return price;
	}
	

	public static Price add(Price price1, Money money) {
		Price price = price1.clone();
		price.setMoney(price1.getMoney() + money.getChargeMoney());
		price.setCoin(price1.getCoin() + money.getCoin());
		price.setPoint(price1.getPoint() + money.getPoint());
		price.setScore(price1.getScore() + money.getScore());
		return price;
	}

	public static void compact(Price p) {
		p.objectId = 0;
		p.objectType  = null;
		p.ownerId = 0;
		p.currentStatus = 0;	
	}


	public boolean isZero() {
		return this.coin == 0 && this.money == 0 && this.score == 0 && this.point == 0;
	}

	/**
	 * 按标准格式解析一个价格 money#coin#point#score
	 * 
	 * 
	 * @author GHOST
	 * @date 2019-01-03
	 */
	public static Price parse(String src) {
		String[] data = src.split("#");
		Price price = new Price();
		price.setMoney(NumericUtils.parseFloat(data[0]));
		price.setCoin(NumericUtils.parseFloat(data[1]));
		price.setPoint(NumericUtils.parseFloat(data[2]));
		price.setScore(NumericUtils.parseLong(data[3]));
		return price;
	}

	
}
