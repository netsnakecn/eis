/**
 * 
 */
package com.maicard.tx.iface;

import com.maicard.core.entity.IndexableEntity;
import com.maicard.tx.entity.Item;

/**
 * 对指定交易品进行规则校验
 * 
 *
 * @author NetSnake
 * @date 2013-9-4 
 */
public interface ProductValidator {
	 
	

	int validate(String action, Item item, IndexableEntity targetObject, Object params);



}
