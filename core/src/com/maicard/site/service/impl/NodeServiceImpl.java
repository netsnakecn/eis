package com.maicard.site.service.impl;

import com.maicard.base.ImplNameTranslate;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ConfigService;
import com.maicard.security.entity.User;
import com.maicard.site.dao.mapper.NodeMapper;
import com.maicard.site.entity.Node;
import com.maicard.site.service.DocumentNodeRelationService;
import com.maicard.site.service.NodeService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

@Service
public class NodeServiceImpl extends AbsGlobalSyncService<Node, NodeMapper> implements NodeService {


	@Resource
	private ConfigService configService;
	@Resource
	private DocumentNodeRelationService documentNodeRelationService;


	private static Map<String,List<Node>> navigationNodeCache = new HashMap<String, List<Node>>();
	@Override
	public int deleteSync(Node entity){

		if(!handleSync() && isMqEnabled()){
			return EisError.NOT_HANDLE_NODE.id;
		}
		//安全删除
		if(entity.getCode() == null){
			entity.setCode(RandomStringUtils.randomAlphabetic(5));
		}
		entity.setCode(entity.getCode() + "_" + entity.getId());
		entity.setDeleted(1);


		int rs = update(entity);
		if(rs == 1 && entity.isCacheable()){
			putCache(entity);
		}
		if (rs == 1 && isMqEnabled()) {
			entity.setSyncFlag(0);
			messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "update", entity);
		}
		return rs;


	}
	public int insert(Node node) {
		Assert.notNull(node,"新增节点为空");
		Assert.notNull(node.getTitle(),"新增节点名称不能为空");
		Assert.notNull(node.getAlias(),"新增节点别名不能为空");
		Assert.isTrue(node.getAlias().matches("[A-Za-z0-9_\\.]+"),"新增节点别名只允许英文字母、数字、下划线或.:" + node.getAlias());
		//Assert.notNull(node.getSiteCode(),"新增节点多站点识别代码不能为空");
		Assert.notNull(node.getProcessor(),"新增节点处理器不能为空");

		if(node.getCurrentStatus() < 1) {
			node.setCurrentStatus(BasicStatus.normal.getId());
		}
		if(StringUtils.isBlank(node.getPath())){
			generateNodePath(node);
			node.setPath(node.getPath().replaceAll("^/", "").replaceAll("/$", ""));
		}
		generateNodeLevel(node);
		int rs = 0;
		rs =  mapper.insert(node);

		if(rs != 1){
			logger.error("无法写入新节点");
			return 0;
		}
		if(node.isCacheable()){
			putCache(node);
		}

		//更新tags
		/*if(node.getTags() == null || node.getTags().equals("")){
			//删除跟之关联的所有tag
			CriteriaMap tagObjectRelationCriteria = new CriteriaMap();
			tagObjectRelationCriteria.put("objectType",ObjectType.node.name());
			tagObjectRelationCriteria.put("objectId",node.getId());
			tagObjectRelationService.delete(tagObjectRelationCriteria);
		} else {
			tagObjectRelationService.sync(node.getOwnerId(), ObjectType.document.toString(), node.getId(), node.getTags());
		}*/
		return 1;
	}

	public int update(Node node) {
		int actualRowsAffected = 0;

		generateNodePath(node);
		node.setPath(node.getPath().replaceAll("^/", "").replaceAll("/$", ""));
			actualRowsAffected = super.update(node);

			if(actualRowsAffected > 0){
				putCache(node);
			}

		logger.debug("更新节点，父节点ID[" + node.getParentNodeId() + "],关联默认模版是:" + node.getTemplateId());

		return actualRowsAffected;
	}




	public List<Node> listInTree(CriteriaMap nodeCriteria) {
		List<Node> plateNodeList = list(nodeCriteria);
		if(plateNodeList == null){
			return null;
		}
		return generateTree(plateNodeList);

	}

	@Override
	public List<Node> generateTree(List<Node> plateNodeList) {
		if(plateNodeList == null){
			return Collections.emptyList();
		}
		for(int i = 0; i< plateNodeList.size(); i++){		
			for(int j = 0; j< plateNodeList.size(); j++){
				if(plateNodeList.get(j).getParentNodeId() == plateNodeList.get(i).getId()){
					//logger.info("Add sub node: " + plateNodeList.get(j).getName() +"["+ plateNodeList.get(j).getId() +"] to parent node:" + plateNodeList.get(i).getName() +"["+ plateNodeList.get(i).getId() + "].");
					if(plateNodeList.get(i).getSubNodeList() == null){
						plateNodeList.get(i).setSubNodeList(new ArrayList<Node>());
					}
					plateNodeList.get(i).getSubNodeList().add(plateNodeList.get(j));
				}
			}

		}
		ArrayList<Node> nodeTree = new ArrayList<Node>();

		for(int i = 0; i< plateNodeList.size(); i++){	
			if(plateNodeList.get(i).getParentNodeId() == 0){
				nodeTree.add(plateNodeList.get(i));
			}
		}
		return nodeTree;

	}



	@Override
	public Node select(String path, long ownerId){
		CriteriaMap nodeCriteria = CriteriaMap.create(ownerId);
		nodeCriteria.put("path",path);
		List<Node> nodeList = list(nodeCriteria);
		if(nodeList == null || nodeList.size() < 1){
			return null;
		}
		return nodeList.get(0);
	}

	@Override
	public Node select(String path, String siteCode, long ownerId){
		CriteriaMap nodeCriteria =  CriteriaMap.create(ownerId);
		nodeCriteria.put("path",path);
		nodeCriteria.put("siteCode",siteCode);
		List<Node> nodeList = list(nodeCriteria);
		if(nodeList == null || nodeList.size() < 1){
			return null;
		}

		return nodeList.get(0);
	}

	//获取从默认节点[首页]开始一直到本节点的路径上的所有节点
	@Override
	public List<Node> getNodePath(long nodeId, long ownerId){
		Node node = select(nodeId);
		if(node == null){
			return null;
		}
		if(node.getOwnerId() != ownerId){
			logger.error("尝试获取的节点[" + node.getId() + "]，其ownerId[" + node.getOwnerId() + "]与系统会话中的[" + ownerId + "]不一致");
			return null;
		}
		ArrayList<Node> pathNodes = new ArrayList<Node>();
		Node fatherNode = select(node.getParentNodeId());
		//logger.info("获取节点[" + node.getId() + "]的路径节点");
		while(fatherNode != null){
			if(fatherNode.getOwnerId() != ownerId){
				logger.error("尝试获取的节点[" + node.getId() + "]的父节点[" + fatherNode.getId() + "，其ownerId[" + fatherNode.getOwnerId() + "]与系统会话中的[" + ownerId + "]不一致");
				return null;
			}
			pathNodes.add(fatherNode);
			//logger.info("查找节点[" + fatherNode.getId() + "]的父节点:" + fatherNode.getParentNodeId());
			if(fatherNode.getParentNodeId() == 0){
				break;
			}
			fatherNode = select(fatherNode.getParentNodeId());

		}
		logger.info("共获取到了" + pathNodes.size() + "个路径节点");
		ArrayList<Node> sortedPathNodes = new ArrayList<Node>();
		//反转顺序
		for(int i = pathNodes.size()-1; i >= 0; i--){
			sortedPathNodes.add(pathNodes.get(i));
		}
		return sortedPathNodes;

	}
	//获取跟本节点同级别的、并且显示到导航上的节点
	public List<Node> getSameLevelNode(int nodeId, int nodeTypeId, boolean removeSelf){
		Node node = select(nodeId);
		if(node == null){
			return null;
		}
		logger.info("查找节点[" + node.getId() + "/" + node.getParentNodeId() + "]类型为:" + nodeTypeId + " 的同级别节点");

		CriteriaMap nodeCriteria = CriteriaMap.create(node.getOwnerId());
		nodeCriteria.put("parentNodeId",node.getParentNodeId());
		nodeCriteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		if(nodeTypeId != 0){
			nodeCriteria.put("nodeTypeId",nodeTypeId);
		}
		List<Node> sameLevelNodeList = list(nodeCriteria);

		if(sameLevelNodeList.contains(node)){
			logger.info("列表中包含自身");
		}
		if(removeSelf){
			for(int i = 0; i < sameLevelNodeList.size(); i++){
				if(sameLevelNodeList.get(i).getId() == nodeId){
					sameLevelNodeList.remove(i);
				}
			}
		}
		return sameLevelNodeList;

	}

	//获取比本节点高一级、并且显示到导航上的节点
	public List<Node> getParentLevelNode(long nodeId, int nodeTypeId){
		Node node = select(nodeId);
		if(node == null){
			return null;
		}
		Node parentNode = select(node.getParentNodeId());
		if(parentNode == null){
			return null;
		}


		CriteriaMap nodeCriteria = CriteriaMap.create(node.getOwnerId());
		nodeCriteria.put("parentNodeId",node.getParentNodeId());
		nodeCriteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		if(nodeTypeId != 0){
			nodeCriteria.put("nodeTypeId",nodeTypeId);
		}
		List<Node> parentLevelNodeList = list(nodeCriteria);

		return parentLevelNodeList;

	}

	/*
	 * 获取比本节点低一级、并且显示到导航上的节点
	 * 如果nodeTypeId != 0 则获取对应类型的node
	 */
	public List<Node> getChildrenLevelNode(long nodeId, int nodeTypeId){
		Node node = select(nodeId);
		if(node == null){
			return null;
		}
		CriteriaMap nodeCriteria = CriteriaMap.create(node.getOwnerId());
		nodeCriteria.put("parentNodeId",node.getId());
		nodeCriteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		if(nodeTypeId != 0){
			nodeCriteria.put("nodeTypeId",nodeTypeId);
		}
		List<Node> childrenLevelNodeList = list(nodeCriteria);

		return childrenLevelNodeList;

	}



	//列出所有(指定类型的)子节点，但跳过目录（即存在下级节点的节点）
	public void listAllChildren(List<Node> all, long  rootNodeId, int nodeTypeId){
		//logger.info("开始循环" + nodeId + "的所有子节点");
		List<Node> child = getChildrenLevelNode(rootNodeId, nodeTypeId);
		if(child == null){
			child = new ArrayList<Node>();
		}
		for(int i = 0; i< child.size(); i++){
			listAllChildren(all, child.get(i).getId(),nodeTypeId);
			//logger.info("child::" + child.get(i).getId());
			all.add(child.get(i));
		}


	}


	@Override
	public List<Node> listPrivilegedNodeByUuid(User user) {
		if(user == null){
			return null;
		}
		if(user.getRelatedPrivilegeList() == null || user.getRelatedPrivilegeList().size() < 1){
			return null;
		}

		//先获取所有的发布权限
		//String privilegeClass = DocumentFormController.class.getName();
		//String privilegeAction = "write";
		//CriteriaMap sysCriteriaMap = CriteriaMap.create(user.getOwnerId());
		//FIXME sysCriteriaMap.setPrivilegeClass(privilegeClass);
		//sysCriteriaMap.setPrivilegeAction(privilegeAction);
		/*List<Privilege> sysPrivilegeList = sysPrivilegeService.list(sysCriteriaMap);
		if(sysPrivilegeList == null || sysPrivilegeList.size() < 1){
			return null;
		}
		//与用户的权限比对
		List<Privilege> userPrivilege = new ArrayList<Privilege>();

		for(int i = 0; i < user.getRelatedPrivilegeList().size(); i++){
			for(int j = 0; j < sysPrivilegeList.size(); j++ ){
				if(user.getRelatedPrivilegeList().get(i).getPrivilegeId() == sysPrivilegeList.get(j).getPrivilegeId()){
					userPrivilege.add(sysPrivilegeList.get(j));
					break;
				}
			}
		}
		 */
		List<Long> nodeIdList = new ArrayList<Long>();
		/*FIXME for(int i = 0; i < userPrivilege.size(); i++){
			if(userPrivilege.get(i).getPrivilegeRelationList() == null || userPrivilege.get(i).getPrivilegeRelationList().size() < 1 ){
				continue;
			}
			for(int j = 0; j < userPrivilege.get(i).getPrivilegeRelationList().size(); j++){
				if(userPrivilege.get(i).getPrivilegeRelationList().get(j).getObjectTypeId() == Constants.ObjectType.node.toString()){
					try{
						nodeIdList.add(Integer.parseInt(userPrivilege.get(i).getPrivilegeRelationList().get(j).getObjectId()));
					}catch(Exception e){}
				}
			}
		}*/
		int nodeIdCount = nodeIdList.size();
		//去重
		HashSet<Long> hs = new HashSet<Long>(nodeIdList);
		nodeIdList.clear();
		nodeIdList.addAll(hs);
		logger.info("共得到用户发布权限相关的节点[" + nodeIdCount + "]个，去重后[" + nodeIdList.size() + "]个" );

		List<Node> userNodes = new ArrayList<Node>();
		for(int i = 0; i < nodeIdList.size(); i++){
			Node node = null;
			try{
				node = mapper.select(nodeIdList.get(i));
			}catch(Exception e){}
			if(node != null){
				userNodes.add(node);
			}

		}
		return userNodes;
	}

	public void generateNodePath(Node node){
		if(node == null){
			throw new EisException(EisError.OBJECT_IS_NULL.id,"生成节点级别时的节点对象为空");
		}
		//写入根节点到当前节点的路径
		if(node.getParentNodeId() == 0){
			//node.setPath(node.getAlias());
			node.setPath("");
		} else {
			Node _parentNode = select(node.getParentNodeId());
			if(_parentNode == null){
				throw new EisException(EisError.OBJECT_IS_NULL.id,"生成节点级别时的节点对象为空");

			}
			//node.setPath(_parentNode.getPath() + "/" + node.getAlias());
			node.setPath((_parentNode.getPath() + "/" + node.getAlias()).replaceFirst("^\\./", "").replaceFirst("^/",""));
		}
	}

	@Override
	public List<Node> generateNavigation(Node rootNode){
		if(rootNode == null){
			return null;
		}
		if(navigationNodeCache == null){
			navigationNodeCache = new HashMap<String, List<Node>>();
		}
		if(navigationNodeCache.size() < 1 || navigationNodeCache.get(rootNode.getSiteCode()) == null){
			CriteriaMap nodeCriteria = CriteriaMap.create(rootNode.getOwnerId());
			nodeCriteria.put("currentStatus", new int[] {BasicStatus.normal.id});
			nodeCriteria.put("parentNodeId",rootNode.getId());
			if(rootNode.getNodeTypeId() > 0){
				nodeCriteria.put("nodeTypeId",rootNode.getNodeTypeId());
			}
			List<Node> childrenLevelNodeList = list(nodeCriteria);

			navigationNodeCache.put(rootNode.getSiteCode(), childrenLevelNodeList);// this.generateTree(childrenLevelNodeList);
		} 
		if(navigationNodeCache.get(rootNode.getSiteCode()) == null){
			return null;
		} else {
			return navigationNodeCache.get(rootNode.getSiteCode());
		}
	}
	public void generateNodeLevel(Node node){
		if(node == null){
			throw new EisException(EisError.OBJECT_IS_NULL.id,"生成节点级别时的节点对象为空");
		}
		if(node.getParentNodeId() == 0){
			//node.setLevel(2);
			if(".".equals(node.getAlias()))
				node.setLevel(0);
			else
				node.setLevel(2);

		} else {
			Node _parentNode = select(node.getParentNodeId());
			if(_parentNode == null){
				throw new EisException(EisError.OBJECT_IS_NULL.id,"生成节点级别时的节点对象为空");

			}
			node.setLevel(_parentNode.getLevel()+1);
		}
	}

	@Override
	public Node getDefaultNode(String siteCode, long ownerId) {
		CriteriaMap nodeCriteria = CriteriaMap.create(ownerId);
		nodeCriteria.put("alias",".");
		nodeCriteria.put("siteCode",siteCode);
		List<Node> nodeList = list(nodeCriteria);
		if(nodeList == null || nodeList.size() < 1){
			return null;
		}

		return nodeList.get(0);
	}



	@Override
	public List<Node> listOnPageClone(CriteriaMap nodeCriteria) {
		List<Node> nodeList = this.listOnPage(nodeCriteria);
		if(nodeList.size() <= 0) {
			return nodeList;
		}
		List<Node> list2 = new ArrayList<Node>(nodeList.size());
		for(Node node : nodeList) {
			list2.add(node.clone());
		}
		return list2;
	}




}
