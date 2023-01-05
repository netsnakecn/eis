package com.maicard.tx.iface;

import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.Pay;
import com.maicard.tx.entity.Order;

/**
 * 订单完成后，针对购买用户的返利计算，及其分销商的返利计算
 */
public interface RebateComputer {

    EisMessage apply(Order order, Pay pay);
}
