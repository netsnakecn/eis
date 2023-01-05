package com.maicard.money.dao.mapper;

import java.util.List;
import java.util.Map;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.Withdraw;

public interface WithdrawMapper extends IDao<Withdraw> {


	int delete(String transactionId) throws DataAccessException;

	Withdraw selectByTx(String transactionId) throws DataAccessException;


	public Withdraw queryByChannelRequestNo(String channelReqNo);
	/**
	 * 人工操作修改订单状态
	 * @param map
	 * @return 修改行数
	 */
	public int updateWithdrawForManualOperate(Map<String,Object> map);

}
