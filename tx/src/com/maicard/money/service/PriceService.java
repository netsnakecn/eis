package com.maicard.money.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

 import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.money.entity.Price;
import com.maicard.money.vo.PriceVo;
import com.maicard.tx.entity.Item;
 
public interface PriceService extends GlobalSyncService<Price> {



	Price getPrice(CriteriaMap priceCriteria); 


	//根据价格类型，创建一个购买token，供后续流程参考
	String generateTransactionToken(Price price, long uuid) throws Exception;

	Price getPriceByToken(BaseEntity object, long uuid, String transactionToken);

	List<Price> bindPrice(HttpServletRequest request, BaseEntity targetObject);

    Map<Long, PriceVo> generateTree(List<Price> priceList);

    Price getPrice(BaseEntity object, String priceType);

	int applyPrice(Item item, Price price);

	int applyPrice(Item item, String string);


	boolean generatePriceExtraData(IndexableEntity object, String priceType);

	boolean generatePriceExtraData(IndexableEntity document, Price price);


    Price applyPriceUseSpecs(Item item, IndexableEntity targetObject);
}
