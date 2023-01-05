package com.maicard.site.service.impl;


import com.alibaba.fastjson.JSON;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.*;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.entity.ExtraData;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.core.exception.EisException;
import com.maicard.core.search.BaseIndexRepository;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerService;
import com.maicard.site.dao.mapper.DocumentMapper;
import com.maicard.site.entity.Document;
import com.maicard.site.entity.DocumentNodeRelation;
import com.maicard.site.entity.DocumentType;
import com.maicard.site.entity.Node;
import com.maicard.site.iface.DocumentPostProcessor;
import com.maicard.site.service.*;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.ObjectMessage;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DocumentServiceImpl extends AbsGlobalSyncService<Document,DocumentMapper> implements DocumentService{
 

	@Resource
	private ApplicationContextService applicationContextService;
	@Resource
	private CertifyService certifyService;
	@Resource
	private ConfigService configService;
	@Resource
	private DataDefineService dataDefineService;
	@Resource
	private DocumentTypeService documentTypeService;
	@Resource
	private DocumentNodeRelationService documentNodeRelationService;
	@Resource
	private NodeService nodeService;
	@Resource
	private PartnerService partnerService;

	
	
	@Autowired(required = false)
	private  BaseIndexRepository baseIndexRepository;
 

	private String EXTRA_DATA_URL_PREFIX;


	static final String DEFAULT_COVER_IMG = "open/default_cover.jpg";

	static final String DEFAULT_AVATAR_IMG = "open/avatar.jpg";

	@PostConstruct
	public void init(){
		if(StringUtils.isBlank(EXTRA_DATA_URL_PREFIX)){
			EXTRA_DATA_URL_PREFIX = "/static";
		}
		logger.debug("附加数据下载URL前缀是:" + EXTRA_DATA_URL_PREFIX);
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public int insert(Document document) {

		if(document.getCreateTime() == null){
			document.setCreateTime(new Date());
		}
		if(document.getValidTime() == null){
			document.setValidTime(new Date());
		}
		if(StringUtils.isBlank(document.getCode())){
			document.setCode(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		}
		if(document.getId() <= 0){
			document.setId(genUuid());
		}
		//清除可能包含的扩展信息request,因为这样会造成无法序列化对象
		HttpServletRequest request = document.removeExtra("request");
		if(request == null) {}
		int rs = 0;
		rs = mapper.insert(document);

		if(rs != 1){
			logger.error("无法写入新文档");
			return -1;
		}
		DocumentType documentType = documentTypeService.select(document.getType());		
		String postProcessorDefine = documentType.getExtra(DataName.documentPostProcessor.name());
		if (StringUtils.isNotBlank(postProcessorDefine)) {
			logger.info("新增文档[" + document.getId() + "/" + document.getType() + "]定义了后期处理器:" + postProcessorDefine);
			DocumentPostProcessor documentPostProcessor = null;
			documentPostProcessor = (DocumentPostProcessor)applicationContextService.getBean(postProcessorDefine);

			if(documentPostProcessor != null){
				int postResult = 0;
				try {
					postResult = documentPostProcessor.process(document, Operate.create.name());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(postResult == 1) {
					//需要更新文档
					mapper.update(document);

				}
			} else {
				String msg = "新增文档[" + document.getId() + "/" + document.getType() + "]定义了后期处理器，但找不到该bean:" + postProcessorDefine;
				logger.error(msg);
				mapper.delete(document.getId());
				throw new EisException(EisError.beanNotFound.id,msg);


			}
		}  


		CriteriaMap documentNodeRelationCriteria = new CriteriaMap();
		documentNodeRelationCriteria.put("udid",document.getId());
		documentNodeRelationService.deleteBy(documentNodeRelationCriteria);
 
		if(document.getRelatedNodeList() != null && document.getRelatedNodeList().size() > 0){
			List<Node> nodeList = document.getRelatedNodeList();
			//然后插入关联节点
			for(int i = 0; i < nodeList.size(); i++){
				DocumentNodeRelation documentNodeRelation = new DocumentNodeRelation();
				documentNodeRelation.setUdid(document.getId());
				documentNodeRelation.setNodeId(nodeList.get(i).getId());
				documentNodeRelation.setCurrentStatus(nodeList.get(i).getCurrentStatus());
				documentNodeRelationService.insert(documentNodeRelation);			
				logger.debug("为新增文档插入与节点[" + documentNodeRelation.getNodeId() + "]的关联关系:" + documentNodeRelation.getCurrentStatus());
			}
		} else {
			logger.warn("新增的文档不包含关联节点,udid=" + document.getId());
		}

		String asyncPpostProcessorDefine = documentType.getExtra(DataName.asyncDocumentPostProcessor.name());
		if (StringUtils.isNotBlank(asyncPpostProcessorDefine)) {
			logger.info("新增文档[" + document.getId() + "/" + document.getType() + "]定义了异步后期处理器:" + asyncPpostProcessorDefine);
			DocumentPostProcessor documentPostProcessor = null;
			documentPostProcessor = (DocumentPostProcessor)applicationContextService.getBean(postProcessorDefine);

			if(documentPostProcessor != null){
				documentPostProcessor.asyncProcess(document, Operate.create.name());

			} else {
				logger.error("新增文档[" + document.getId() + "/" + document.getType() + "]定义了后期处理器，但找不到该bean" + postProcessorDefine);

			}
		}
		//this.createIndex(document);
		return 1;

	}
	
	@Override
	public void createIndex(IndexableEntity document) {
		logger.debug("为文档:{}创建索引:{}", document.getId(), JSON.toJSONString(document));
		if(baseIndexRepository == null) {
			logger.error("无法创建索引，因为没有注册baseIndexResponsitory");
			return;
		}
		baseIndexRepository.save(document);

	}
	
	@Override
	public void deleteIndex(long id) {
		
		logger.debug("删除文档:{}的索引", id);
		try {
			baseIndexRepository.deleteById(id);
		}catch(IndexNotFoundException e) {
			
		}catch(Exception e2) {
			e2.printStackTrace();
		}
	}




	@Override
	public int update(Document entity){
		if(entity == null){
			return -1;
		}
		long udid = entity.getId();
		if(udid < 1){
			return -1;
		}
		entity.setValidTime(entity.getPublishTime());

		//Document _oldDocument = mapper.select(udid);
		//清除可能包含的扩展信息request,因为这样会造成无法序列化对象
		HttpServletRequest request = entity.removeDto("request");
		if(request == null) {}

		int rs = super.update(entity);

		if(rs != OpResult.success.id){
			return EisError.DATA_UPDATE_FAIL.id;
		}


		CriteriaMap documentNodeRelationCriteria = new CriteriaMap();
		documentNodeRelationCriteria.put("udid",entity.getId());
		documentNodeRelationService.deleteBy(documentNodeRelationCriteria);

		if(entity.getRelatedNodeList() != null && entity.getRelatedNodeList().size() > 0){
			logger.warn("更新的文档[" + entity.getId() + "]需要更新" + entity.getRelatedNodeList().size() + "个关联节点");

			List<Node> nodeList = entity.getRelatedNodeList();
			//然后插入关联节点
			for(int i = 0; i < nodeList.size(); i++){
				DocumentNodeRelation documentNodeRelation = new DocumentNodeRelation();
				documentNodeRelation.setUdid(entity.getId());
				documentNodeRelation.setNodeId(nodeList.get(i).getId());
				documentNodeRelation.setCurrentStatus(nodeList.get(i).getCurrentStatus());
				documentNodeRelationService.insert(documentNodeRelation);			
			}
		}else {
			logger.warn("更新的文档不包含关联节点udid=" + entity.getId());
		}

		logger.info("更新文档和扩展数据完毕");
		 putCache(entity);
		return rs;
	}



	@Override
	public 	int changeStatus(long id, int status) throws Exception{
		Document document = mapper.select(id);
		if(document == null){
			return 0;
		}
		document.setCurrentStatus(status);
		return update(document);

	}









	@Override
	public boolean postProcess(ObjectMessage objectMessage) throws Exception{
		Document document = null;
		try{
			document = (Document)objectMessage.getObject();
		}catch(Exception e){}
		if(document == null){
			logger.error("无法对空对象文档进行后期处理.");
			return false;
		}
		/*if(document.getCurrentStatus() == DocumentStatus.inProgress.getId()){
			if(pdf2swf(document)){
				//处理成功，将状态转换为之前的状态
				document.setCurrentStatus(document.getFlag());
			} else {
				document.setCurrentStatus(CommonStandard.Error.dataError.getId());
			}		
			changeStatus(document.getId(), document.getCurrentStatus());
		}*/
		return false;
	}



	@Override
	public Document select(String documentCode, long ownerId) {
		CriteriaMap documentCriteria = CriteriaMap.create(ownerId);
		documentCriteria.putArray("documentCodes",documentCode);
		List<Long> udidList= mapper.listPk(documentCriteria);
		logger.debug("[" + documentCode + "]得到的文章数量是:" + (udidList == null ? -1 : udidList.size()));
		if(udidList == null || udidList.size() != 1){
			return null;
		}
		Document document = new Document();
		document.setId(udidList.get(0));
		return select(document);
	}


	@Override
	public void afterFetch(Document document) {

		//logger.debug("获取文档:{}的关联和扩展数据",document.getId());

		//查找对应的自定义数据
		CriteriaMap  criteria = new CriteriaMap();
		criteria.put("udid",document.getId());
		List<DocumentNodeRelation> documentNodeRelationList = documentNodeRelationService.list(criteria);
		ArrayList<Node> nodeList = new ArrayList<Node>();
		if(documentNodeRelationList.size() > 0){
			for(int i = 0; i < documentNodeRelationList.size(); i++){
				Node node = nodeService.select(documentNodeRelationList.get(i).getNodeId());
				if(node == null){
					continue;
				}
				//用对应关系表中的状态取代输出的节点状态
				node.setCurrentStatus(documentNodeRelationList.get(i).getCurrentStatus());
				if(node.getCurrentStatus()  == BasicStatus.defaulted.getId()){
					document.setDefaultNode(node);
				}
				nodeList.add(node);			
			}
			document.setRelatedNodeList(nodeList);
		}

	}
	/*@Override
	public void processDataPath(Document document) {
		if(document == null){
			return;
		}
		if(document.getExtraDataMap() == null || document.getExtraDataMap().size() < 1){
			return;
		}
		for(ExtraData documentData : document.getExtraDataMap().values()){
			if(documentData == null){
				continue;
			}
			documentData.setId(documentData.getExtraDataId());
			if(documentData.getInputMethod() != null && documentData.getInputMethod().equals("file")){
				if(documentData.getDataValue() != null && !documentData.getDataValue().startsWith(EXTRA_DATA_URL_PREFIX)){
					if(documentData.getDisplayLevel() == null){
						documentData.setDataValue(EXTRA_DATA_URL_PREFIX + "/" + CommonStandard.EXTRA_DATA_OPEN_PATH + "/" + documentData.getDataValue());
					} else {
						if(documentData.getDisplayLevel().equals(DisplayLevel.subscriber.name())){
							documentData.setDataValue(EXTRA_DATA_URL_PREFIX + "/" + CommonStandard.EXTRA_DATA_SUBSCRIBE_PATH + "/" + documentData.getDataValue());
						} else if(documentData.getDisplayLevel().equals(DisplayLevel.login.name())){
							documentData.setDataValue(EXTRA_DATA_URL_PREFIX + "/" + CommonStandard.EXTRA_DATA_LOGIN_PATH + "/" + documentData.getDataValue());
						} else {
							documentData.setDataValue(EXTRA_DATA_URL_PREFIX + "/" + CommonStandard.EXTRA_DATA_OPEN_PATH + "/" + documentData.getDataValue());
						}
					}
				}
			}

		}
	}*/


}
