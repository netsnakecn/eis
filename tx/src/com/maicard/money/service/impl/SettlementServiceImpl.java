package com.maicard.money.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.constants.TxStatus;
import com.maicard.money.dao.mapper.SettlementMapper;
import com.maicard.money.entity.Settlement;
import com.maicard.money.service.SettlementService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SettlementServiceImpl extends AbsGlobalSyncService<Settlement, SettlementMapper> implements SettlementService {

 

	public int insert(CriteriaMap settlementCriteria) {
		try{
			return mapper.insert(settlementCriteria);
		}catch(Exception e){
			logger.error("插入数据失败:" + e.getMessage());
		}
		return -1;
	}

	public int update(CriteriaMap settlementCriteria) {

		List<Settlement> settlementList = mapper.list(settlementCriteria);

		for(int i = 0; i < settlementList.size(); i++){			
			Settlement _oldSettlement = mapper.select(settlementList.get(i).getId());
			if (_oldSettlement != null) {
				_oldSettlement.setCurrentStatus(TxStatus.success.getId());
				try{
					mapper.update(_oldSettlement);
				}catch(Exception e){
					logger.error("更新数据失败:" + e.getMessage());
					return 0;
				}
			}	

		}	
		return 1;
	}




	public int countrecentbilling(CriteriaMap settlementCriteria) {
		return mapper.countrecentbilling(settlementCriteria);
	}


}
