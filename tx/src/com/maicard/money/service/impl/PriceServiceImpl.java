package com.maicard.money.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.vo.PriceVo;
import com.maicard.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.ServletRequestUtils;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.dao.mapper.PriceMapper;
import com.maicard.money.entity.Price;
import com.maicard.money.service.PriceService;
import com.maicard.tx.entity.Item;

@Service
public class PriceServiceImpl extends AbsGlobalSyncService<Price,PriceMapper> implements PriceService {



	@Resource
	private ApplicationContextService applicationContextService;
	@Resource
	private ConfigService configService;
	@Resource
	private MessageService messageService;

	final DecimalFormat defaultMoneyFormat = new DecimalFormat("0.##");

	private String aesKey = null;
	private int priceTtl = 1800;

	static final String CACHE_NAME = CacheNames.cacheNameProduct;

	@PostConstruct
	public void init(){
		try {
			aesKey = SecurityUtils.readAesKey();
		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}

	}


	@Override
	public List<Price> bindPrice(HttpServletRequest request, BaseEntity targetObject) {

		String availablePriceType = configService.getValue(DataName.availablePriceType.name(), targetObject.getOwnerId());
		if(StringUtils.isBlank(availablePriceType)) {
			availablePriceType = Price.PRICE_STANDARD;
		}

		List<Price> priceList = new ArrayList<Price>();
		float marketPrice = ServletRequestUtils.getFloatParameter(request, "marketPrice", 0);
		logger.info("当前获取到的marketPrice={}", marketPrice);
		String[] priceTypeList =  availablePriceType.split(",");
		for(String priceType : priceTypeList) {
			Price price = new Price(priceType);
			price.setMarketPrice(marketPrice);
			float money = ServletRequestUtils.getFloatParameter(request, priceType + ".money", 0f);
			if(money > 0) {
				price.setMoney(money);
			}
			float coin = ServletRequestUtils.getFloatParameter(request, priceType + ".coin", 0f);
			if(coin > 0) {
				price.setCoin(coin);
			}
			float point = ServletRequestUtils.getFloatParameter(request, priceType + ".point", 0f);
			if(point > 0) {
				price.setPoint(point);
			}
			long score = ServletRequestUtils.getLongParameter(request, priceType + ".score", 0);
			if(score > 0) {
				price.setScore(score);
			}
			if(!price.isZero()) {
				priceList.add(price);
			}
		}

		return priceList;
	}





	@Override
	public int applyPrice(Item item, Price price){
		if(price.getMoney() <= 0 && price.getCoin() <= 0 && price.getPoint() <= 0 && price.getScore() <= 0){
			logger.warn("价格[" + price + "]中的所有资金都是0");
			//return EisError.moneyRangeError.getId();
		}
		if(item.getCount() < 1){
			logger.error("当前交易[" + item.getTransactionId() + "]的购买数量是0");
			return EisError.COUNT_IS_ZERO.getId();
		}
		item.setLabelMoney(price.getMoney());
		item.setRequestMoney(price.getMoney() * item.getCount());
		item.setFrozenMoney(price.getCoin()* item.getCount());
		item.setSuccessMoney(price.getPoint()* item.getCount());
		item.setInMoney(price.getScore()* item.getCount());
		item.setBillingStatus((int)price.getId());
		Price p = price.clone();
		Price.compact(p);
		item.setPrice(p);
		logger.info("经计算，[" + item.getObjectType() + "/" + item.getProductId() + "]的价格规则是[" + price + "]，设置交易[" + item.getTransactionId() + "]的labelMoney为单价:" + item.getLabelMoney() + ",requestMoney为现金money:" + item.getRequestMoney() + ",frozenMoney为coin:" + item.getFrozenMoney() + ",successMoney为点数point:" + item.getSuccessMoney());
		return OpResult.success.getId();
	}

	@Override
	public boolean generatePriceExtraData(IndexableEntity object, String priceType) {
		CriteriaMap criteria = CriteriaMap.create(object.getOwnerId());
		criteria.put("objectType",object.getEntityType());
		criteria.put("objectId",object.getId());
		criteria.putArray("currentStatus",BasicStatus.normal.getId());
		List<Price> priceList = list(criteria);
		Map<Long,PriceVo> topMap = generateTree(priceList);
		String text = JsonUtils.toStringFull(topMap.values());
		logger.info("为产品:" + object.getId() + "/" + object.getEntityType() + "生成价格树:" + text);
		object.setExtra("priceMap",text);
		Price price = getPrice(object, priceType);
		if(price == null) {
			return false;
		}
		return generatePriceExtraData(object, price);

	}

	@Override
	public Map<Long,PriceVo> generateTree(List<Price> priceList) {
		if(priceList == null || priceList.size() < 1){
			return Collections.emptyMap();

		}
		//生成规格树
		Map<Long,PriceVo> topMap = new HashMap<Long, PriceVo>();
		for(Price p : priceList){
			if(p.getParentId() == 0){
				topMap.put(p.getId(),PriceVo.of(p));
			}
		}
		//按照级别排序，级别高的排前面
		for(Price p : priceList){
			if(p.getParentId() > 0){
				if(topMap.containsKey(p.getParentId())){
					topMap.get(p.getParentId()).sub.add(PriceVo.of(p));
				}
			}
		}
		return topMap;

	}


	@Override
	public Price getPrice(BaseEntity object, String priceType) {

		Assert.notNull(object,"尝试获取价格的对象不能为空");
		Assert.notNull(object.getEntityType(),"尝试获取价格的对象类型不能为空");
		Assert.isTrue(object.getId() > 0,"尝试获取价格的对象ID不能为空");

		CriteriaMap priceCriteria = CriteriaMap.create(object.getOwnerId());
		priceCriteria.put("objectType",object.getEntityType());
		priceCriteria.put("objectId",object.getId());
		priceCriteria.putArray("currentStatus",BasicStatus.normal.getId());
		priceCriteria.put("priceType",priceType);
		List<Price> priceList = list(priceCriteria);
		if( priceList.size() < 1){
			try {
				logger.info("找不到objectType={},id={}的价格规则,使用反射检查{}类的getPrice方法", object.getEntityType(), object.getId(), object.getClass().getName());
				Method method = ClassUtils.getMethodIfAvailable(object.getClass(), "getPrice");
				if(method == null) {
					return null;
				}
				Object result = method.invoke(object);
				if(result != null) {
					//按标准逻辑解析价格字符串 money#coin#point#score					
					return Price.parse(result.toString());
				}
			} catch ( Exception e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return priceList.get(0);
		}
	}


	@Override
	public boolean generatePriceExtraData(IndexableEntity document, Price price) {


/*
		StringBuffer sb = new StringBuffer();
		//sb.append("PRICE_");
		//sb.append(price.getPriceType());
		sb.append(MoneyType.money.name());
		sb.append('=');
		sb.append(price.getMoney());
		sb.append('&');
		sb.append(MoneyType.coin.name());
		sb.append('=');
		sb.append(price.getCoin());
		sb.append('&');
		sb.append(MoneyType.point.name());
		sb.append('=');
		sb.append(price.getPoint());
		sb.append('&');
		sb.append(MoneyType.score.name());
		sb.append('=');
		sb.append(price.getScore());
		sb.append('&');
		document.setExtraValue("PRICE_" + price.getPriceType(), sb.toString().replaceAll("&$",""));

		sb = new StringBuffer();			
		if(price.getMoney() > 0){
			sb.append("money:");
			sb.append(price.getMoney());
			sb.append(';');
		}
		if(price.getCoin() > 0){
			sb.append("coin:");
			sb.append(price.getCoin());
			sb.append(';');
		}
		if(price.getPoint() > 0){
			sb.append("point:");
			sb.append(price.getPoint());
			sb.append(';');
		}
		if(price.getScore() > 0){
			sb.append("score:");
			sb.append(price.getScore());
			sb.append(';');
		}
		*/

		document.setExtra(DataName.productMarketPrice.toString(), String.valueOf(price.getMarketPrice()));
		document.setExtra(price.getPriceType(), String.valueOf(price.getMoney()));
		return true;
	}

	@Override
	public Price applyPriceUseSpecs(Item item, IndexableEntity targetObject) {
		long priceId = item.getLongExtra(DataName.priceId.name());
		if(priceId <= 0){
			logger.error("交易:" + item.getTransactionId() + "中没有扩展数据priceId");
			return null;
		}

		Price price = select(priceId);
		if(price == null){
			logger.error("根据交易:" + item.getTransactionId() + "指定的priceId=" + priceId + "找不到任何价格");
			return null;
		}
		if(!price.getObjectType().equalsIgnoreCase(targetObject.getEntityType()) || price.getObjectId() != targetObject.getId()){
			logger.error("交易:" + item.getTransactionId() + "指定的价格:" + JsonUtils.toStringFull(price) + "与商品类型或ID不一致:" + JsonUtils.toStringFull(targetObject));
			return null;
		}
		this.applyPrice(item,price);
		return price;



	}

	@Override
	public int applyPrice(Item item, String priceType) {

		logger.info("开始为订单:{}应用价格:{}", item.getTransactionId(), priceType);

		Assert.notNull(item, "尝试应用价格的Item是空");
		Assert.isTrue(item.getChargeFromAccount() > 0, "尝试应用价格的Item，未指定用户");
		Assert.isTrue(item.getProductId() > 0, "尝试应用价格的Item，未指定产品Id");
		Assert.notNull(priceType, "尝试应用价格的Item，未指定价格类型priceType");


		CriteriaMap priceCriteria = CriteriaMap.create(item.getOwnerId());
		priceCriteria.put("objectType",item.getObjectType());
		priceCriteria.put("objectId",item.getProductId());
		priceCriteria.put("priceType",priceType);
		priceCriteria.putArray("currentStatus",BasicStatus.normal.getId());
		List<Price> priceList = list(priceCriteria);
		if(priceList == null || priceList.size() < 1){
			logger.warn("找不到{}/{}的价格规则", item.getObjectType(), item.getProductId());
			return EisError.SYSTEM_DATA_ERROR.getId();
		}
		if(priceList.size() != 1){
			logger.warn("{}/{}的类型={}的价格规则不唯一", item.getObjectType(), item.getProductId(), priceType);
			return EisError.SYSTEM_DATA_ERROR.getId();
		}
		Price price = priceList.get(0);
		return applyPrice(item, price);
	}


	@Override
	public Price getPrice(CriteriaMap priceCriteria) {

		Assert.notNull(priceCriteria,"尝试获取价格的条件不能为空");
		long objectId = priceCriteria.getLongValue("objectId");
		Assert.isTrue(objectId > 0, "尝试获取价格的条件，其价格对象ID不能为空");
		if(!priceCriteria.containsKey("priceType")){
			logger.error("尝试获取价格的条件未指定价格类型，使用默认类型:" + Price.PRICE_STANDARD);
		} else {
			/*boolean validPriceType = false;
			for(PriceType pt : PriceType.values()){
				if(priceCriteria.getPriceType().equalsIgnoreCase(pt.toString())){
					validPriceType = true;
					break;
				}
			}
			if(!validPriceType){
				logger.error("无效的价格类型:" + priceCriteria.getObjectType() + "，使用默认价格类型");
				priceCriteria.setPriceType(PriceType.PRICE_STANDARD.toString());
			}*/
		}
		if(priceCriteria.getStringValue("objectType") == null){
			logger.error("尝试获取价格的条件未指定价格对象类型，使用默认类型document");
			priceCriteria.put("objectType",ObjectType.document.name());
		}

		priceCriteria.putArray("currentStatus",BasicStatus.normal.getId());
		List<Price> priceList = list(priceCriteria);
		if(priceList == null || priceList.size() < 1){
			logger.warn("找不到类型=" + priceCriteria.getStringValue("objectType") + "ID=" + objectId + "的" + priceCriteria.getStringValue("priceType") + "价格规则");
			return null;
		}
		if(priceList.size() != 1){
			logger.warn("类型=" + priceCriteria.getStringValue("objectType") + "ID=" + objectId + "的" + priceCriteria.getStringValue("priceType") + "价格规则不唯一");
			return null;
		}
		return priceList.get(0);
	}



	@Override
	public String generateTransactionToken(Price price, long uuid) throws Exception {
		long ts = new Date().getTime();
		String src = new StringBuffer().append(price.getId()).append('|').append(price.getObjectId()).append('|').append(uuid).append('|').append(ts).toString();
		logger.debug("根据价格[" + price.getId() + "]、产品[" + price.getObjectId() + "]和用户UUID[" + uuid + "]生成交易令牌源:" + src);
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		return crypt.aesEncryptHex(src);
	}


	@Override
	public Price getPriceByToken(BaseEntity object, long uuid, String transactionToken) {
		Assert.notNull(object, " 尝试通过令牌获取价格的对象不能为空");
		Assert.isTrue(object.getId() > 0, "尝试通过获取价格的objectId不能为0");
		if(uuid < 1){
			logger.error("尝试获取价格的uuid异常:" + uuid);
			return null;
		}
		if(StringUtils.isBlank(transactionToken)){
			logger.error("尝试获取价格的transactionToken为空");
			return null;
		}
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		String src = null;
		try {
			src = new String(crypt.aesDecryptHex(transactionToken),Constants.DEFAULT_CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(src == null){
			logger.error("无法对transactionToken进行解密:" + transactionToken);
			return null;
		}
		String[] data = src.split("\\|");
		if(data == null || data.length < 5){
			logger.error("无法对解密数据进行分组:" + src);
			return null;
		}
		String objectType = data[1];
		if(!objectType.equalsIgnoreCase(object.getEntityType())) {
			logger.error("交易令牌中的第2个数据[" + data[1] + "]与objectType[" + object.getEntityType() + "]不一致");
			return null;
		}
		long objectId = NumericUtils.parseLong(data[2]);

		if(objectId <= 0 || objectId != object.getId()){
			logger.error("交易令牌中的第3个数据[" + data[2] + "]与objectId[" + objectId + "]不一致");
			return null;
		}
		if(!NumericUtils.isNumeric(data[2])){
			logger.error("交易令牌数据异常，第3个数据不是数字:" + src);
			return null;
		}
		if(!data[2].equals(String.valueOf(uuid))){
			logger.error("交易令牌中的第3个数据[" + data[2] + "]与uuid[" + uuid + "]不一致");
			return null;
		}
		if(!NumericUtils.isNumeric(data[3])){
			logger.error("交易令牌数据异常，第4个数据不是数字:" + src);
			return null;
		}
		long tokenTs = Long.parseLong(data[3]);
		long currentTs = new Date().getTime();
		logger.debug("交易令牌时间是[" + StringTools.ts2String(tokenTs) + "]，当前价格有效期是" + priceTtl + "秒");
		if(currentTs - tokenTs  > priceTtl * 1000){
			logger.error("交易令牌时间[" + StringTools.ts2String(tokenTs) + "]已超过有效期" + priceTtl + "秒");
		}
		if(!data[2].equals(String.valueOf(uuid))){
			logger.error("交易令牌中的第3个数据[" + data[2] + "]与uuid[" + uuid + "]不一致");
			return null;
		}
		long priceId = 0;
		if(!NumericUtils.isNumeric(data[0])){
			logger.error("交易令牌数据异常，第一个数据不是数字");
		}
		priceId = Long.parseLong(data[0]);
		Price price = select(priceId);
		if(price == null){
			logger.error("根据交易令牌找不到指定的Price:" + priceId);
			return null;
		}
		if(price.getIdentify() != null){

		}

		return price;


	}

}
