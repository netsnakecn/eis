package com.maicard.money.service.abs;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.dao.mapper.ShareConfigMapper;
import com.maicard.money.entity.ShareConfig;
import com.maicard.money.service.ShareConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
 

@Service
public class AbsShareConfigServiceImpl extends AbsGlobalSyncService<ShareConfig,ShareConfigMapper> implements ShareConfigService {


	
	@Resource
	private MessageService messageService;



	@Override
	//根据UUID和产品ID计算应得的分成比例
	public ShareConfig calculateShare(CriteriaMap shareConfigCriteria) {
		Assert.notNull(shareConfigCriteria,"尝试计算分成比例的shareConfigCriteria不能为空");
		Assert.notNull(shareConfigCriteria.getLongValue("shareUuid") != 0,"尝试计算分成比例的shareConfigCriteria，其shareUuid不能为0");
		List<ShareConfig> shareConfigList = list(shareConfigCriteria);
		if(shareConfigList != null && shareConfigList.size() > 0){
			return shareConfigList.get(0);
		}
		return null;
	}
  

	@Override
	public EisMessage clone(CriteriaMap shareConfigCriteria) {
		
		CriteriaMap shareConfigCriteria2 = shareConfigCriteria.clone();
		List<ShareConfig> shareConfigList = this.list(shareConfigCriteria2);
		if(shareConfigList == null || shareConfigList.size() < 1){
			logger.error("无法克隆分成配置，因为获取[shareUuid=ownerId=" + shareConfigCriteria2.getLongValue("shareUuid")+ "]的分成配置为空");
			return EisMessage.error(EisError.REQUIRED_PARAMETER.id,"找不到系统分成配置");
		}

		List<ShareConfig> newShareConfigList = new ArrayList<ShareConfig>();
		logger.info("uuid=ownerId=" + shareConfigCriteria2.getLongValue("shareUuid") + "的分成配置数量有" + shareConfigList.size() + "条");
		//为确保更新，先删除该用户所有分成
		this.deleteByUuid(shareConfigCriteria.getLongValue("shareUuid"));
		for(ShareConfig oldShareConfig : shareConfigList){
			
			ShareConfig newShareConfig = oldShareConfig.clone();

				newShareConfig.setId(0);
				newShareConfig.setShareUuid(shareConfigCriteria.getLongValue("shareUuid"));
				
				
				int rs = this.insert(newShareConfig);
				logger.info("从分成配置[" + oldShareConfig + "]新克隆了分成配置[" + newShareConfig + "],数据插入结果:" + rs);
				if(rs != 1){
					logger.error("无法创建新菜单[" + newShareConfig + "]，返回是:" + rs);
					return EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"无法新增分成配置");
				} else {
					messageService.sendJmsDataSyncMessage(null, "shareConfigService", "insert", newShareConfig);
				}

		}
		
		EisMessage resultMsg = EisMessage.success("克隆完成");
		resultMsg.setExtra("shareConfigList", newShareConfigList);
		return resultMsg;	}

	private void deleteByUuid(long shareUuid) {
		mapper.deleteByUuid(shareUuid);
	}

	@Override
	public ShareConfig calculateShare(Object obj, CriteriaMap shareConfigCriteria) {
		logger.warn("标准实现不支持本方法，直接使用不带第一个参数的方法: calculateShare(shareConfigCriteria)");
		return calculateShare(shareConfigCriteria);
	}

}

