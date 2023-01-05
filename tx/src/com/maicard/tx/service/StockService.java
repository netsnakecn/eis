package com.maicard.tx.service;

import com.maicard.core.entity.IndexableEntity;
import com.maicard.security.entity.User;
import com.maicard.site.entity.Document;
import com.maicard.tx.constants.StockMode;
import com.maicard.tx.constants.StockType;
import com.maicard.tx.dto.ItemDto;
import com.maicard.tx.entity.Item;
import com.maicard.tx.vo.FeeVo;
import com.maicard.tx.vo.StockVo;

import java.util.List;

public interface StockService {
	StockVo getStock(String stockTarget, String shopId, long objectId);

	StockVo lock(StockVo lockModel);

	int release(StockVo... stockList);

	IndexableEntity writeItemData(Item item, String objectType, long objectId);


	IndexableEntity getTargetObject(String objectType, long objectId);


	int checkPrivilege(Document document, User frontUser);

	boolean setStock(StockVo setModel, StockMode stockMode);

	/**
	 * 改变一个商品的库存，负数为减少
	 * @return 返回修改后的最新数值
	 * 
	 * 
	 * @author GHOST
	 * @date 2018-11-19
	 */
	float addStock(String objectType, String shopId, long objectId, float offset);


	IndexableEntity getTargetObject(String objectType, String objectCode, long ownerId);


    FeeVo computerFee(List<ItemDto> itemList);
}
