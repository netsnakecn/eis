package com.maicard.money.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.money.entity.Settlement;
 

public interface SettlementMapper extends IDao<Settlement> {

	

	List<Settlement> listRecentBilling(CriteriaMap settlementCriteria) throws DataAccessException;
		
	int countrecentbilling(CriteriaMap settlementCriteria) throws DataAccessException;

	int insert(CriteriaMap settlementCriteria);

}
