package com.maicard.site.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.print.Doc;

import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.site.dao.mapper.DocumentTypeMapper;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.base.Pair;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.iface.TypeService;
import com.maicard.core.service.DataDefineService;
import com.maicard.site.entity.DocumentType;
import com.maicard.site.service.DocumentTypeService;

@Service
public class DocumentTypeServiceImpl extends AbsBaseService<DocumentType,DocumentTypeMapper> implements DocumentTypeService,TypeService {

 
	@Resource
	private DataDefineService dataDefineService;

	public int insert(DocumentType documentType) {
		int rs = 0;
		try{
			rs = mapper.insert(documentType);
		}catch(Exception e){
			logger.error("插入数据失败:" + e.getMessage());
		}
		if(rs != 1){
			logger.error("新增文档类型失败,数据操作未返回1");
			return -1;
		}

		if(documentType.getDataDefineMap() != null){
			if(documentType.getDataDefineMap().size() > 0){
				//保险起见，即使是新增也先尝试删除一遍
				CriteriaMap dataDefineCriteria = new CriteriaMap();
				 
				for(int i = 0; i < documentType.getDataDefineMap().size(); i++){
					documentType.getDataDefineMap().get(i).setObjectType(ObjectType.document.toString());
					documentType.getDataDefineMap().get(i).setObjectId(documentType.getId());
					documentType.getDataDefineMap().get(i).setCurrentStatus(BasicStatus.normal.getId());
					dataDefineService.insert(documentType.getDataDefineMap().get(i));
				}
			}
		}

		return 1;
	}

	public int update(DocumentType documentType) {
		//logger.info("更新时的大小:" + documentType.getDocumentDataDefineMap().size());
		int actualRowsAffected = 0;

		long documentTypeId = documentType.getId();

		DocumentType _oldDocumentType = mapper.select(documentTypeId);

		if (_oldDocumentType == null) {
			return 0;
		}
		try{
			actualRowsAffected = mapper.update(documentType);
		}catch(Exception e){
			logger.error("更新数据失败:" + e.getMessage());

		}

		logger.info("update documentColumnList:" + documentType.getDataDefineMap().size());
		if(documentType.getDataDefineMap() == null || documentType.getDataDefineMap().size() < 1){
			//没有任何关联关系，应当清空
			CriteriaMap dataDefineCriteria = new CriteriaMap();
			dataDefineCriteria.put("objectType",ObjectType.document.name());
			dataDefineCriteria.put("objectId",documentType.getId());
			//dataDefineService.delete(dataDefineCriteria);
		} else if(documentType.getDataDefineMap() != null && documentType.getDataDefineMap().size() > 0){
			//FIXME 	dataDefineService.sync(documentType.getDataDefineMap());

			//			for(int i = 0; i < documentType.getDocumentDataDefineMap().size(); i++){
			//				documentType.getDocumentDataDefineMap().get(i).setDocumentTypeId(documentType.getId());
			//				if(documentType.getDocumentDataDefineMap().get(i).getCurrentStatus() != CommonStandard.Operate.delete.getId()){
			//					documentType.getDocumentDataDefineMap().get(i).setCurrentStatus(CommonStandard.BasicStatus.normal.getId());
			//				}
			//				documentDataDefineService.sync(documentType.getDocumentDataDefineMap().get(i));
			//			}

		} 
		return actualRowsAffected;
	}




	@Override
	public void afterFetch(DocumentType documentType){
		if(documentType == null){
			return;
		}
		documentType.setId(documentType.getId());
		CriteriaMap dataDefineCriteria = new CriteriaMap(); 
		dataDefineCriteria.put("objectType",ObjectType.document.toString());
		dataDefineCriteria.put("objectId",documentType.getId());
		Map<String, DataDefine> documentDataDefineMap = dataDefineService.map(dataDefineCriteria);
		if(documentDataDefineMap != null){
			logger.debug("该文档类型[" + documentType.getId() + "]包含" + documentDataDefineMap.size() + "个自定义字段");
			documentType.setDataDefineMap(documentDataDefineMap);
		}
	}



	@Override
	public DocumentType select(String documentTypeCode) {
		CriteriaMap documentTypeCriteria = new CriteriaMap();
		documentTypeCriteria.put("documentTypeCode",documentTypeCode);
		List<DocumentType> documentTypeList = list(documentTypeCriteria);
		if(documentTypeList != null && documentTypeList.size() > 0){
			return documentTypeList.get(0);
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Pair> typeList(CriteriaMap criteria) {
		List<DocumentType> documentTypeList = list(criteria);
		List<Pair> list = new ArrayList<Pair>(documentTypeList.size());
		for(DocumentType dt : documentTypeList) {
			Pair p = new Pair(dt.getId(), dt.getDocumentTypeName());
			list.add(p);
		}
		return list;
	}

	@Override
	public String getObjectTypeName() {
		return "document";
	}

	@Override
	public Map<Integer, String> initExtraTypes() {
 		return null;
	}



}
