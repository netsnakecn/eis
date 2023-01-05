package com.maicard.security.service.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.annotation.Resource;

import com.maicard.base.BaseVo;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.constants.MessageBusEnum;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.Role;
import com.maicard.security.service.PartnerPrivilegeService;
import com.maicard.security.vo.UserVoSimple;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.entity.ExtraData;
import com.maicard.core.entity.GlobalUnique;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.mb.service.MessageService;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.dao.mapper.PartnerMapper;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRoleRelation;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerRoleService;
import com.maicard.security.service.PartnerService;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.SecurityUtils;


@Service
public class PartnerServiceImpl extends AbsGlobalSyncService<User, PartnerMapper> implements PartnerService {

	@Resource
	private ConfigService configService;
	@Resource
	private DataDefineService dataDefineService;
	@Resource
	private MessageService messageService;
	@Resource
	private GlobalUniqueService globalUniqueService;

	@Resource
	private PartnerRoleRelationService partnerRoleRelationService;
	@Resource
	private PartnerRoleService partnerRoleService;

	@Resource
	private PartnerPrivilegeService partnerPrivilegeService;


	protected MessageBusEnum messageBusEnum = MessageBusEnum.USER;

	
	@Override
	public void setForOperator(CriteriaMap criteria, User partner) {
		Assert.notNull(criteria,"尝试设置操作员条件不能为空");
		Assert.notNull(partner,"尝试设置操作员条件时用户不能为空");
		criteria.remove("inviter");
		if(partner.getParentUuid() > 0) {
			criteria.putArray("inviters", partner.getParentUuid());
		} else {
			criteria.putArray("inviters", partner.getUuid());
		}
	}

	@Override
	public int insert(User partner) {
		Assert.notNull(partner,"尝试插入的partner对象为空");
		GlobalUnique unique = new GlobalUnique(partner.getUsername(),partner.getOwnerId());
		if(globalUniqueService.exist(unique)){
			logger.error("无法创建Partner用户，因为全局数据不唯一[" + partner.getUsername() + "]");
			return -1;
		}
		if(!globalUniqueService.create(unique)){
			logger.error("无法创建Partner用户，因为无法创建全局唯一数据[" + partner.getUsername() + "]");
			return -1;
		}
		String inviteByCode = partner.getExtra("USER_INVITE_BY_CODE");
		if(StringUtils.isNotBlank(inviteByCode)){
			inviteByCode = inviteByCode.trim();
			CriteriaMap params = CriteriaMap.create();
			params.put("userTypeId",UserTypes.partner.id);
			params.put("dataCode", "USER_INVITE_CODE");
			params.put("dataValue",inviteByCode);
			params.put("correctWithDynamicData",false);
			List<ExtraData> userDataList = null;// userDataService.list(params);
			if(userDataList != null && userDataList.size() == 1 ){
				logger.info("根据邀请码[" + inviteByCode + "]查找对应的推广ID:" + userDataList.get(0).getUuid());
				long inviteUuid = userDataList.get(0).getUuid();
				partner.setInviter(inviteUuid);
				partner.setParentUuid(inviteUuid);
			} else {
				logger.info("根据邀请码[" + inviteByCode + "]找不到对应的推广ID.");
			}
		}

		int rs = insertLocal(partner);	
		if(rs != 1){
			logger.error("本地插入Partner失败,回滚唯一数据，返回值:" + rs);
			//失败了删除唯一数据
			globalUniqueService.delete(unique);
			return rs;
		}
		//发送新增用户的同步信息到消息总线
		if(partner.getSyncFlag() == 1){
			return 1;
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("发送新增合作伙伴用户[" + partner.getUuid() + "]的同步信息到消息总线");
			}
			messageService.sendJmsDataSyncMessage(messageBusEnum.name(), "partnerService", "insertLocal", partner);
		}
		return 1;

	}
	@Override
	public int insertLocal(User partner) {
		if(StringUtils.isBlank(partner.getUserPassword())){
			logger.error("合作伙伴没有设置密码");
			return -1;
		}
		if(partner.getUserPassword().length() != 64){//防止二次加密
			//将密码改为加密形式
			String plainPassword = partner.getUserPassword();
			partner.setUserPassword(SecurityUtils.passwordEncode(partner.getUserPassword()));
			logger.debug("新增合作伙伴密码是:" + plainPassword + "/" + partner.getUserPassword());
		}
		if(partner.getAuthKey() != null && partner.getAuthKey().length() != 64){//防止二次加密
			//将密码改为加密形式
			String plainPassword = partner.getAuthKey();
			partner.setAuthKey(SecurityUtils.passwordEncode(partner.getAuthKey()));
			logger.debug("新增合作伙伴二级密码是:" + plainPassword + "/" + partner.getAuthKey());
		}
		
		if(partner.getLevel() == 0){
			partner.setLevel(2);
		}
		if(partner.getCreateTime() == null){
			partner.setCreateTime(new Date());
		}
		if(partner.getUserTypeId() <= 0) {
			partner.setUserTypeId(UserTypes.partner.id);
		}int rs = 0;
		try{
			rs = mapper.insert(partner);
		}catch(Exception e){
			logger.error("插入数据失败:" + e.getMessage());
		}
		if(rs != 1){
			logger.error("插入数据失败,数据操作未返回1");
			return rs;	
		}
		logger.debug("新增合作伙伴UUID=" + partner.getUuid() + ",userTypeId=" + partner.getUserTypeId());
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("uuid", partner.getUuid());
		partnerRoleRelationService.deleteBy(criteria);
		if(partner.getRelatedRoleList() != null){
			logger.debug("为新增合作伙伴[" + partner.getUuid() + "]增加关联角色:" + partner.getRelatedRoleList().size() );
			for(int i = 0; i < partner.getRelatedRoleList().size(); i++){
				UserRoleRelation userRoleRelation = new  UserRoleRelation();
				userRoleRelation.setCurrentStatus(BasicStatus.normal.id);
				userRoleRelation.setUuid(partner.getUuid());
				userRoleRelation.setRoleId(partner.getRelatedRoleList().get(i).getId());
				userRoleRelation.setOwnerId(partner.getOwnerId());
				partnerRoleRelationService.insert(userRoleRelation);
			}
		} else {
			int partnerDefaultRoleId = configService.getIntValue("partner.defaultRoleId",0);
			if(partnerDefaultRoleId > 0){
				logger.info("新增的合作伙伴[" + partner.getUuid() + "]未包含对应角色，为其添加默认角色:" + partnerDefaultRoleId);
				UserRoleRelation userRoleRelation = new  UserRoleRelation();
				userRoleRelation.setCurrentStatus(BasicStatus.normal.id);
				userRoleRelation.setUuid(partner.getUuid());
				userRoleRelation.setRoleId(partnerDefaultRoleId);
				partnerRoleRelationService.insert(userRoleRelation);
			} else {
				logger.warn("新增的合作伙伴[" + partner.getUuid() + "]未包含对应角色，系统也未配置添加默认角色:" + partnerDefaultRoleId);

			}
		}
		try{
			//userDataService.processUserConfig(partner);
		}catch(Exception e){
			throw new EisException(EisError.DATA_ERROR.id,"在处理用户配置数据时出错:" + e.getMessage());
		}	
		return 1;
	}



	 

	@Override
	public int updateLocal(User partner) {
		if(partner == null){
			return 0;
		}
		int actualRowsAffected = 1;
		long uuid = partner.getUuid();	
		User _oldPartner = mapper.select(uuid);
		//partner.setUserTypeId(UserTypes.partner.id);
		if (_oldPartner != null) {
			if(partner.getUserPassword() == null || partner.getUserPassword().equals("")){
				//未修改密码
				partner.setUserPassword(_oldPartner.getUserPassword());
			} else {
				//与原密码不一致
				if(!partner.getUserPassword().equals(_oldPartner.getUserPassword())){
					if(partner.getUserPassword().length() != 64){//防止二次加密
						//将密码改为加密形式
						String plainPassword = partner.getUserPassword();
						partner.setUserPassword(SecurityUtils.passwordEncode(partner.getUserPassword()));
						if(logger.isDebugEnabled()){
							logger.debug("修改用户密码是:" + plainPassword + "/" + partner.getUserPassword());
						}
					}
				}
			}
			if(logger.isDebugEnabled()){
				logger.debug("更新用户，密码是:" + partner.getUserPassword() + "/" + partner.getCurrentStatus());
			}
			try{
				mapper.update(partner);
			}catch(Exception e){
				logger.error("更新数据失败:" + e.getMessage());
			}
		}
		/*if(partner.getUserData() != null && partner.getUserData().size() > 0) {
			try{
				userDataService.processUserConfig(partner);
			}catch(Exception e){
				throw new EisException(EisError.DATA_ERROR.id,"在处理用户配置数据时出错:" + e.getMessage());
			}
		}*/

		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("uuid", partner.getUuid());
		partnerRoleRelationService.deleteBy(criteria);
		if(partner.getRelatedRoleList() != null){
			logger.debug("更新合作伙伴[" + partner.getUuid() + "]增加关联角色:" + partner.getRelatedRoleList().size() );
			for(int i = 0; i < partner.getRelatedRoleList().size(); i++){
				UserRoleRelation userRoleRelation = new  UserRoleRelation();
				userRoleRelation.setCurrentStatus(BasicStatus.normal.id);
				userRoleRelation.setUuid(partner.getUuid());
				userRoleRelation.setRoleId(partner.getRelatedRoleList().get(i).getId());
				partnerRoleRelationService.insert(userRoleRelation);
			}
		}
		return actualRowsAffected;
	}

	@Override
	public int delete(long uuid) {
		User _oldUser = mapper.select(uuid);
		if(_oldUser == null){
			logger.warn("尝试删除的Partner[" + uuid + "]不存在");
			return 0;
		}
		if(_oldUser.isCacheable()){
			removeCache(_oldUser);
		}
		int rs = mapper.delete(uuid);
		if(rs != 1){
			logger.error("本地删除Partner[" + uuid + "]失败,返回值:" + rs);
			return rs;
		}
		//删除Partner后，应当删除GlobalData
		GlobalUnique unique = new GlobalUnique(_oldUser.getUsername(),_oldUser.getOwnerId());
		globalUniqueService.delete(unique);


		return 1;
	}



	//列出指定partner的所有子账户
	public void listAllChildren(List<User> all, long  fatherId){
		//logger.debug("开始循环" + fatherId + "的所有子账户");
		List<User> child = getChildren(fatherId);
		if(child == null){
			child = new ArrayList<User>();
		}
		for(int i = 0; i< child.size(); i++){
			listAllChildren(all, child.get(i).getUuid());
			//logger.info("child::" + child.get(i).getUuid());
			all.add(child.get(i));
		}

	}
	private List<User> getChildren(long uuid){
		User partner = select(uuid);
		if(partner == null){
			return Collections.emptyList();
		}

		CriteriaMap partnerCriteria = CriteriaMap.create();
		partnerCriteria.put("parentUuid",uuid);
		List<Long> pkList = mapper.listPk(partnerCriteria);
		if(pkList != null && pkList.size() > 0){
			List<User> childrenList = new ArrayList<User>();
			for(Long subUuid : pkList){
				User child = mapper.select(subUuid);
				if(child != null){
					childrenList.add(child);
				}
			}
			return childrenList;
		} else {
			return Collections.emptyList();
		}
	}

	public int count(CriteriaMap partnerCriteria){
		return mapper.count(partnerCriteria);
	}

	//获取当前用户直到2级用户（即总合作伙伴账户]的所有用户
	public List<User> getUserPath(long uuid){
		User user = select(uuid);
		if(user == null){
			return Collections.emptyList();
		}
		ArrayList<User> pathUsers = new ArrayList<User>();
		User fatherUser = select(user.getParentUuid());
		//logger.info("获取节点[" + node.getNodeId() + "]的路径节点");
		while(fatherUser != null){
			pathUsers.add(fatherUser);
			//logger.info("查找节点[" + fatherNode.getNodeId() + "]的父节点:" + fatherNode.getParentNodeId());
			if(fatherUser.getLevel() <= 2){
				break;
			}
			fatherUser = select(fatherUser.getParentUuid());

		}
		logger.info("共获取到了" + pathUsers.size() + "个上级用户");
		ArrayList<User> sortedPathUsers = new ArrayList<User>();
		//反转顺序
		for(int i = pathUsers.size()-1; i >= 0; i--){
			sortedPathUsers.add(pathUsers.get(i));
		}
		return sortedPathUsers;

	}

	@Override
	public boolean isValidSubUser(long parentUuid, long childUuid) {
		List<User>all = new ArrayList<User>();
		listAllChildren(all, parentUuid);
		if(all == null || all.size() < 1){
			return false;
		}
		for(User user : all){
			if(user.getUuid() == childUuid){
				return true;
			}
		}

		return false;
	}

	@Override
	public List<User> listBelowUser(long rootUuid, long ownerId) {
		String uuids = mapper.listBelowUser(rootUuid);
		if(StringUtils.isBlank(uuids)){
			return Collections.emptyList();
		}
		String[] uuidArray = uuids.split(",");
		List<User> belowUserList = new ArrayList<User>();
		for(String uuid : uuidArray){
			if (StringUtils.isBlank(uuid)) {
				continue;
			}
			long id = Long.parseLong(uuid);
			if(id <= 0){
				continue;
			}
			User child = new User();
			child.setId(id);
			child = select(child);
			if(child == null || child.getOwnerId() != ownerId){
				continue;
			}
			belowUserList.add(child);

		}

		return belowUserList;
	}




	@Override
	public void afterFetch(User partner){
		if(partner == null){
			return;
		}
		if(partner.getNickName() == null || partner.getNickName().equals("")){
			partner.setNickName(partner.getUsername());
		}
		CriteriaMap params = CriteriaMap.create(partner.getOwnerId());
		params.put("objectId",partner.getUuid());
		params.put("objectExtraId",partner.getUserExtraTypeId());

		//partner.setUserData(userDataService.map(params));

		List<Privilege> privilegeList = new ArrayList<Privilege>();
		params.init();
		params.put("uuid",partner.getUuid());
		List<UserRoleRelation> sysRoleUserRelationList = partnerRoleRelationService.list(params);
		logger.info("合作伙伴[" + partner.getUuid() + "/" + partner.getUsername() + "]拥有的关联角色数量是" + (sysRoleUserRelationList == null ? "空" : sysRoleUserRelationList.size()));
		if(sysRoleUserRelationList == null || sysRoleUserRelationList.size() < 1){
			partner.setRelatedPrivilegeList(Collections.<Privilege>emptyList());
			return;
		}

		ArrayList<Role> relatedRoleList = new ArrayList<Role>();
		int[] roleIds = new int[sysRoleUserRelationList.size()];
		for(int i = 0; i < sysRoleUserRelationList.size(); i++){
			logger.debug("查找合作伙伴[" + partner.getUuid() + "/" + partner.getUsername() + "]对应角色[" + sysRoleUserRelationList.get(i).getRoleId() + "]");
			roleIds[i] = sysRoleUserRelationList.get(i).getRoleId();
			Role partnerRole = partnerRoleService.select(sysRoleUserRelationList.get(i).getRoleId());
			if(partnerRole != null){
				logger.debug("找到了合作伙伴[" + partner.getUuid() + "/" + partner.getUsername() + "]对应角色[" + partnerRole.getId() + "/" + partnerRole.getRoleName() + "]");
				relatedRoleList.add(partnerRole);
			}
		}
		params.clear();
		params.put("currentStatus",BasicStatus.normal.id);
		params.put("roleIds",roleIds);
		privilegeList = partnerPrivilegeService.listByRole(params);

		partner.setRelatedRoleList(relatedRoleList);
		//权限去重
		//int before = privilegeList.size();
		HashSet<Privilege> hs = new HashSet<Privilege>(privilegeList);
		privilegeList.clear();
		privilegeList.addAll(hs);
		partner.setRelatedPrivilegeList(privilegeList);
	}

	@Override
	public long getHeadUuid(User partner){

		Assert.notNull(partner,"尝试获取祖先账户的用户不能为空");
		if(partner.getHeadUuid() > 0){
			logger.debug("当前商户[" + partner.getUuid() + "]已有祖先账户ID:" + partner.getHeadUuid());
			return partner.getHeadUuid();
		}
		List<User> userList = this.getUserPath(partner.getUuid());
		if(userList == null || userList.size() < 1){
			if(partner.getParentUuid() > 0){
				logger.debug("当前商户[" + partner.getUuid() + "]没有祖先账户，返回其父账户ID作为祖先账户ID:" + partner.getParentUuid());
				return partner.getParentUuid();
			} else {
				logger.debug("当前商户[" + partner.getUuid() + "]没有祖先账户也没有父账户，返回其自身账户ID作为祖先账户ID:" + partner.getUuid());
				return partner.getUuid();
			}
		}
		long headUuid = userList.get(0).getUuid();
		logger.debug("经过计算，当前商户[" + partner.getUuid() + "]的祖先账户ID是:" + headUuid);
		return headUuid;
	}

	@Override
	public long getHeadUuid(long inviter){
		if(inviter <= 0){
			return 0;
		}
		User partner = select(inviter);
		if(partner == null){
			logger.error("找不到指定的partner:" + inviter);
			return 0;
		}
		return getHeadUuid(partner);
	}
 

	
	@Override
	public void correctUserAttributes(User user){
		Assert.notNull(user,"尝试检查的用户对象为空");
		Assert.isTrue(user.getUuid() > 0,"尝试检查的用户对象UUID为空");


		User _oldUser = mapper.select(user.getUuid());
		//将不允许自行修改的项目设置为旧的
		user.setOwnerId(_oldUser.getOwnerId());
		user.setUsername(_oldUser.getUsername());
		user.setCreateTime(_oldUser.getCreateTime());
		user.setInviter(_oldUser.getInviter());
		user.setUserTypeId(UserTypes.partner.id);
		user.setLevel(_oldUser.getLevel());
		user.setCurrentStatus(_oldUser.getCurrentStatus());
	}

	@Override
	public User selectOne(CriteriaMap partnerCriteria) {
		if(partnerCriteria.containsKey("currentStatus")) {
			partnerCriteria.fixToArray("currentStatus");
		}
		List<User> userList = list(partnerCriteria);
		if(userList.size() < 1) {
			return null;
		} else {
			return userList.get(0);
		}
	}
	
	
	@Override
	public User fuzzySelect(CriteriaMap criteria) {
		Object username = criteria.remove("username");
		
		if(username == null) {
			//并没有查询用户名
			return new User();
		}
		if(NumericUtils.isNumeric(username)) {
			long uuid = NumericUtils.parseLong(username);
			return select(uuid);
		} else {
			//尝试使用username查询
			criteria.put("fuzzyName", username);
			User u = this.selectOne(criteria);
			criteria.remove("fuzzyName");
			return u;
		}
	}

	@Override
	public void setSubPartner(CriteriaMap criteria, User partner, boolean addSelf) {
		
		if(partner.getBelowUser() == null || partner.getBelowUser().size() < 1) {
			partner.setBelowUser( BaseVo.toVoList(UserVoSimple.class,listBelowUser(partner.getUuid(), partner.getOwnerId())));
		}
		List<UserVoSimple> belowUserList = partner.getBelowUser();
		StringBuffer sb = new StringBuffer();

		List<Long> inviterList = new ArrayList<Long>();

		//没放入自己
		if(addSelf) {
			inviterList.add(partner.getUuid());
		}
		sb.append(partner.getUuid()).append(',');
		if(belowUserList != null && belowUserList.size() > 0){
			for(int i = 0; i < belowUserList.size(); i++){
				inviterList.add(belowUserList.get(i).getUuid());
				sb.append(belowUserList.get(i).getUuid()).append(',');
			}
		}		

		long[] inviters = NumericUtils.longList2Array(inviterList);
		if(inviters.length < 1) {
			//必须放入一个条件，否则可能造成该条件无限大
			inviters = new long[] {-1};
		}	
		if(logger.isDebugEnabled())logger.debug("当前partner[" + partner.getUuid() + "]的子账户列表:" + Arrays.toString(inviters));

		criteria.put("inviters",inviters);
	}



	@Override
	@CacheEvict(value=CacheNames.cacheNameUser, key = "'Partner#' + #partner.uuid")
	public void evictCache(User partner) {
		logger.info("清除Partner:{}的缓存数据", partner.getUuid());
	}
	
	@Override
	public List<User> listSimplePartnerByExtraType(User partner, int... extraTypeIds){
		
		
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("userTypeId", UserTypes.partner.id);
		criteria.put("userExtraTypeIds", extraTypeIds);
		criteria.putArray("currentStatus", UserStatus.normal.id);
		
		return listSimplePartner(criteria);
	}
	
	@Override
	public List<User> listSimplePartner(CriteriaMap criteria){
		List<User> partnerList = listOnPage(criteria);
		if(partnerList.size() < 1) {
			return partnerList;
		}
		
		List<User> userList = new ArrayList<User>(partnerList.size());
		for(User p : partnerList) {
			User user = new User();
			user.setUuid(p.getUuid());
			user.setId(p.getId());
			user.setUsername(p.getUsername());
			user.setNickName(p.getNickName());
			user.setHeadUuid(p.getHeadUuid());
			user.setParentUuid(p.getParentUuid());
			user.setInviter(p.getInviter());
			userList.add(user);
		}
		return userList;
		
	}
}
