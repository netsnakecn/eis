package com.maicard.money.iface;

import com.maicard.base.CriteriaMap;

/**
 * 计算资金平衡表的接口，不同平台可以有不同的实现和计算方法
 *
 *
 * @author NetSnake
 * @date 2017-11-05
 */
public interface MoneyBalanceComputer {
	
	/**
	 * 计算当前时刻的资金平衡
	 * @param moneyBalanceCriteria
	 * @return
	 */
	public int computer(CriteriaMap moneyBalanceCriteria);

}
