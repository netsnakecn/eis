package com.maicard.site.service.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import org.springframework.stereotype.Service;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.annotation.IgnoreJmsDataSync;
import com.maicard.site.dao.mapper.DocumentNodeRelationMapper;
import com.maicard.site.entity.DocumentNodeRelation;
import com.maicard.site.service.DocumentNodeRelationService;

@Service
public class DocumentNodeRelationServiceImpl extends AbsBaseService<DocumentNodeRelation,DocumentNodeRelationMapper> implements DocumentNodeRelationService {

	@Resource
	private DocumentNodeRelationMapper documentNodeRelationMapper;
	
	@Resource
	private ConfigService configService;
	


	@IgnoreJmsDataSync
	public int insert(DocumentNodeRelation documentNodeRelation) {
		return documentNodeRelationMapper.insert(documentNodeRelation);
		
	}


	@Override
	@IgnoreJmsDataSync
	public int deleteBy(CriteriaMap documentNodeRelationCriteria) {
		
		if(documentNodeRelationCriteria == null){
			throw new EisException(EisError.PARAMETER_ERROR.id,"删除条件为空");
		}
		if(documentNodeRelationCriteria.getLongValue("id") == 0 && documentNodeRelationCriteria.getLongValue("udid") == 0 && documentNodeRelationCriteria.getLongValue("nodeId") == 0){
			throw new EisException(EisError.PARAMETER_ERROR.id,"删除条件为空");
		}
		
		int allCount = documentNodeRelationMapper.count(new CriteriaMap());
		List<DocumentNodeRelation> _oldDocumentNodeRelationList = documentNodeRelationMapper.list(documentNodeRelationCriteria);
		if(_oldDocumentNodeRelationList == null || _oldDocumentNodeRelationList.size() < 1){
			return 0;
		}
		if(_oldDocumentNodeRelationList.size() == allCount){
			logger.error("按删除条件操作将删除所有记录");
			return 0;
		}
		int realDeleted = documentNodeRelationMapper.deleteBy(documentNodeRelationCriteria);
		logger.debug("应删除[" + _oldDocumentNodeRelationList.size() + "]条记录，实际删除[" + realDeleted + "]条记录");
		long[]affectedNodeIds = new long[_oldDocumentNodeRelationList.size()];
		for(int i = 0; i < _oldDocumentNodeRelationList.size(); i++){
			affectedNodeIds[i] = _oldDocumentNodeRelationList.get(i).getNodeId();
		}		
		return realDeleted;
	}

	@Override
	public int delete(long id) {
		return  documentNodeRelationMapper.deleteBy(CriteriaMap.create().put("id",id));
	}


	public DocumentNodeRelation select(int documentNodeRelationId) {
		DocumentNodeRelation documentNodeRelation  = documentNodeRelationMapper.select(documentNodeRelationId);
		return documentNodeRelation;

	}

	public List<DocumentNodeRelation> list(CriteriaMap documentNodeRelationCriteria) {
		List<DocumentNodeRelation> documentNodeRelationList = documentNodeRelationMapper.list(documentNodeRelationCriteria);
		if(documentNodeRelationList == null) {
			return Collections.emptyList();
		}
		return documentNodeRelationList;
	}
	
	public List<DocumentNodeRelation> listOnPage(CriteriaMap documentNodeRelationCriteria) {
		List<DocumentNodeRelation> documentNodeRelationList = documentNodeRelationMapper.list(documentNodeRelationCriteria);
		if(documentNodeRelationList == null) {
			return Collections.emptyList();
		}
		return documentNodeRelationList;
	}

	

}
