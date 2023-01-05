package com.maicard.boss.controller.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.CacheService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.annotation.RequestPrivilege;
import com.maicard.security.constants.RoleTypes;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.MenuRoleRelation;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.PrivilegeRoleRelation;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRoleRelation;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerMenuRoleRelationService;
import com.maicard.security.service.PartnerPrivilegeRoleRelationService;
import com.maicard.security.service.PartnerPrivilegeService;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerRoleService;
import com.maicard.security.service.PartnerService;

@Controller
@RequestMapping("/partnerRole")
public class PartnerRoleController extends ValidateBaseController {
	@Resource
	private CertifyService certifyService;
	@Resource
	private PartnerRoleService partnerRoleService;
	@Resource
	private PartnerPrivilegeService partnerPrivilegeService;
	@Resource
	private PartnerRoleRelationService partnerRoleRelationService;
	@Resource
	private PartnerPrivilegeRoleRelationService partnerPrivilegeRoleRelationService;
	@Resource
	private PartnerService partnerService;
	 
	
	@Resource
	private PartnerMenuRoleRelationService partnerMenuRoleRelationService;
	
	@Resource
	private CacheService cacheService;


	@RequestMapping(method=RequestMethod.GET)	
	public String list(HttpServletRequest request, HttpServletResponse response, ModelMap map, @RequestParam Map<String, Object> params) throws Exception {
		
		
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		String roleLevelStr = ServletRequestUtils.getStringParameter(request, "roleLevel", null);
		if(StringUtils.isBlank(roleLevelStr) && !isPlatformGenericPartner) {
			logger.error("非内部用户列出角色但未提交roleLevel");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleLevel"));
			return ViewNames.partnerMessageView;

		}
		
		CriteriaMap criteria =  CriteriaMap.create(params);
//		if(!isPlatformGenericPartner) {
			//不是内部用户，检查其能选择的角色
			criteria.putArray("roleLevels", partnerRoleService.getValidLevel(partner, roleLevelStr));
			criteria.remove("roleLevel");
	//	}
		int totalRows = partnerRoleService.count(criteria);
		List<Role> partnerRoleList = partnerRoleService.list(criteria);
		
		map.put("total", totalRows);
		map.put("rows",partnerRoleList);
		map.put("operateMap",getOperateMap(partner, "partnerRole"));


		return "common/partnerRole/index";

	}

	@RequestMapping(value="/get" + "/{roleId}", method=RequestMethod.GET )		
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap map,		@PathVariable("roleId")Long roleId) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		if(roleId == 0){
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.getId(), "缺少必须的参数roleId"));
			return ViewNames.partnerMessageView;
		}
		
		Role partnerRole = partnerRoleService.select(roleId);
		if(partnerRole == null){
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的role对象"));
			return ViewNames.partnerMessageView;
		}	
		if(partnerRole.getOwnerId() != partner.getOwnerId()){
			logger.error("PartnerRole[" + partnerRole.getId() + "]的ownerId[" + partnerRole.getOwnerId() + "]不属于当前ownerId:" + partner.getOwnerId());
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的role对象"));
			return ViewNames.partnerMessageView;		
		}
		//获取与角色关联的权限
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleIds", new long[] {roleId});
		List<Privilege> rolePrivilegeList = partnerPrivilegeService.listByRole(criteria);
		logger.debug("与" + roleId + "#角色对应的权限有:" + (rolePrivilegeList == null ? "空" : rolePrivilegeList.size()));
		//获取与角色关联的用户
		List<UserRoleRelation> userRoleRelationList = partnerRoleRelationService.list(criteria);
		if(userRoleRelationList == null || userRoleRelationList.size() < 1){
			logger.info("没有任何用户关联到角色:" + roleId);
		} else {
			List<User> partnerList = new ArrayList<User>();
			for(UserRoleRelation userRoleRelation : userRoleRelationList){
				User p = partnerService.select(userRoleRelation.getUuid());
				if(p == null){
					logger.error("找不到与" + roleId + "#角色关联的parnter:" + userRoleRelation.getUuid());
					continue;
				}
				if(p.getOwnerId() != partner.getOwnerId()){
					logger.error("找与" + roleId + "#角色关联的parnter:" + userRoleRelation.getUuid() + "，其ownerId与当前用户[" + partner.getOwnerId() + "]的不一致");
					continue;
				}
				partnerList.add(p);
			}
			map.put("partnerList", partnerList);

		}
		map.put("remainPrivilegeList", partnerPrivilegeService.listRemainPrivilege(rolePrivilegeList));
		map.put("role", partnerRole);
		map.put("rolePrivilegeList",rolePrivilegeList);
		map.put("operateMap", this.getOperateMap(partner, "partnerRole"));
		criteria.init();
		criteria.put("roleIds", new long[] {partnerRole.getId()});
 		List<Menu> menuList = partnerMenuRoleRelationService.listAllByRole(criteria); 
		map.put("roleMenuList", menuList);
		return "common/partnerRole/detail";
	}

	@RequestMapping(value="/delete", method=RequestMethod.POST)	
	public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@RequestParam("idList") String idList) throws Exception {
		if(idList == null || idList.equals("")){
			throw new EisException(EisError.REQUIRED_PARAMETER.id,"请求中找不到必须的参数[idList]");
		}
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		

		String[] ids = idList.split("-");
		int successDeleteCount = 0;
		String errors = "";
		for(int i = 0; i < ids.length; i++){
			int deleteId = Integer.parseInt(ids[i]);
			Role partnerRole = partnerRoleService.select(deleteId);
			if(partnerRole == null){
				logger.warn("找不到要删除的PartnerRole，ID=" + deleteId);
				continue;
			}

			if(partnerRole.getOwnerId() != partner.getOwnerId()){
				logger.warn("要删除的PartnerRole，ownerId[" + partnerRole.getOwnerId() + "]与系统会话中的ownerId不一致:" + deleteId);
				continue;
			}
			try{
				if(partnerRoleService.delete(Integer.parseInt(ids[i])) > 0){
					successDeleteCount++;
				}
			}catch(DataIntegrityViolationException forignKeyException ){
				String error  = " 无法删除[" + ids[i] + "]，因为与其他数据有关联. ";
				logger.error(error);
				errors += error + "\n";
			}
		}

		String messageContent = "成功删除[" + successDeleteCount + "]个.";
		if(!errors.equals("")){
			messageContent += errors;
		}
		logger.info(messageContent);
		map.put("message", EisMessage.success(OpResult.success.getId(),messageContent));
		return ViewNames.partnerMessageView;
	}

	@RequestMapping(value="/create"+"/{parentRoleId}", method=RequestMethod.GET)	
	public String getCreate(HttpServletRequest request, HttpServletResponse response,ModelMap map, 	@PathVariable("parentRoleId") int parentRoleId) throws Exception {
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		Role parentRole  = partnerRoleService.select(parentRoleId);
		if(parentRole == null){

			logger.error("找不到父角色:" + parentRoleId);
			return ViewNames.partnerMessageView;		
		}
		if(parentRole.getOwnerId() != partner.getOwnerId()){
			logger.error("找不到父角色:" + parentRoleId);
			return ViewNames.partnerMessageView;		
		}
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleType",RoleTypes.ROLE_TYPE_DEPARTMENT);
		criteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		//类型为1即为组织单元的组，而非2岗位
		List<Role> unitRoleList = partnerRoleService.list(criteria);
		List<Privilege> partnerPrivilegeList = new ArrayList<Privilege>();

		map.put("unitRoleList", unitRoleList);
		map.put("statusCodeList", BasicStatus.values());
		Role partnerRole = null;

		partnerRole = new Role();
		//	int parentRoleId = ServletRequestUtils.getIntParameter(request, "parentRoleId", 0);
		partnerRole.setParentRoleId(parentRoleId);
		logger.info("设置新增组父ID=" + parentRoleId);	
		criteria.init();
		criteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		partnerPrivilegeList = partnerPrivilegeService.list(criteria);

		map.put("partnerPrivilegeList", partnerPrivilegeList);
		map.put("partnerRole", partnerRole);
		return "common/partnerRole/" + "create";
	}



	@RequestMapping(value="/create",method=RequestMethod.POST)
	@AllowJsonOutput
	public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			Role partnerRole) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		partnerRole.setOwnerId(partner.getOwnerId());
		//获取额外的权限关系
		int[] enabledPrivilege = ServletRequestUtils.getIntParameters(request, "partnerPrivilege");
		EisMessage message = null;
		try{
			int rs = partnerRoleService.insert(partnerRole);
			if(rs != 1){
				map.put("message", EisMessage.error(EisError.DATA_ERROR.id,"无法添加"));
				return ViewNames.partnerMessageView;
			}
			if(partnerRole.getRoleType() == RoleTypes.ROLE_TYPE_USER.id){
				for(int i = 0; i < enabledPrivilege.length; i++){
					PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation();
					privilegeRoleRelation.setOwnerId(partner.getOwnerId());
					privilegeRoleRelation.setPrivilegeId(enabledPrivilege[i]);
					privilegeRoleRelation.setRoleId((int)partnerRole.getId());
					partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
				}
				
			} 
		}catch(Exception e){
			throw new EisException(EisError.DATA_UPDATE_FAIL.id,"数据操作失败" + e.getMessage());
		}
		map.put("message",message);
		return ViewNames.partnerMessageView;

	}


	@RequestMapping(value="/update", method=RequestMethod.POST)
	@AllowJsonOutput
	public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@ModelAttribute("partnerRole") Role partnerRole) throws Exception {
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		Role _oldPartnerRole = partnerRoleService.select((int)partnerRole.getId());
		if(_oldPartnerRole == null){
			logger.error("找不到要修改的PartnerRole，ID=" + partnerRole.getId());
			return ViewNames.partnerMessageView;		
		}
		if(_oldPartnerRole.getOwnerId() != partner.getOwnerId()){
			logger.warn("要修改的PartnerRole，ownerId[" + _oldPartnerRole.getOwnerId() + "]与系统会话中的ownerId不一致:" + partner.getOwnerId());
			return ViewNames.partnerMessageView;		
		}
		partnerRole.setOwnerId(_oldPartnerRole.getOwnerId());
		//获取额外的权限关系
		int[] enabledPrivilege = ServletRequestUtils.getIntParameters(request, "partnerPrivilege");
		EisMessage message = null;
		try{
			int rs = partnerRoleService.update(partnerRole);
			if(rs != 1){
				map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"无法更新"));
				return ViewNames.partnerMessageView;
			}
			if(partnerRole.getRoleType() == RoleTypes.ROLE_TYPE_USER.id){
				for(int i = 0; i < enabledPrivilege.length; i++){
					PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation();
					privilegeRoleRelation.setOwnerId(partner.getOwnerId());
					privilegeRoleRelation.setPrivilegeId(enabledPrivilege[i]);
					privilegeRoleRelation.setRoleId( (int)partnerRole.getId());
					partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
				}
				
			} 
		}catch(Exception e){
			throw new EisException(EisError.DATA_UPDATE_FAIL.id,"数据操作失败" + e.getMessage());
		}
		map.put("message",message);
		return ViewNames.partnerMessageView;

	}

	@RequestMapping(value="/deletePrivilegeRelation", method=RequestMethod.POST)	
	@AllowJsonOutput
	@RequestPrivilege(object="partnerRole",operate="update")
	public String deletePrivilegeRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
		if(roleId <= 0) {
			logger.error("删除关联但未提交roleId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
			return ViewNames.partnerMessageView;

		}
		
		
		long privilegeId = ServletRequestUtils.getLongParameter(request, "privilegeId", 0);
		if(privilegeId <= 0) {
			logger.error("删除关联但未提交privilegeId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
			return ViewNames.partnerMessageView;

		}
		
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleId",roleId);
		criteria.put("privilegeId", privilegeId);
		int rs = partnerPrivilegeRoleRelationService.deleteBy(criteria);

		if(rs > 0) {
			cacheService.evict(CacheNames.cacheNameUser);
			map.put("message", EisMessage.success(OpResult.success.getId(),"成功删除权限关联"));
		} else {
			map.put("message", EisMessage.error(OpResult.failed.getId(),"删除权限关联失败"));
		}
		return ViewNames.partnerMessageView;
	}
	
	
	
	/**
	 * 添加一个角色和权限的关联
	 * 
	 */
	@RequestMapping(value="/addPrivilegeRelation", method=RequestMethod.POST)	
	@AllowJsonOutput
	@RequestPrivilege(object="partnerRole",operate="update")
	public String addPrivilegeRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
		if(roleId <= 0) {
			logger.error("添加关联但未提交roleId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
			return ViewNames.partnerMessageView;

		}
		
		
		long privilegeId = ServletRequestUtils.getLongParameter(request, "privilegeId", 0);
		if(privilegeId <= 0) {
			logger.error("添加关联但未提交privilegeId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
			return ViewNames.partnerMessageView;

		}
		
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleId",roleId);
		criteria.put("privilegeId", privilegeId);
		int cnt = partnerPrivilegeRoleRelationService.count(criteria);

		if(cnt > 0) {
			logger.warn("已存在roleId={},privilegeId={}的关联关系",roleId, privilegeId);
			map.put("message", EisMessage.error(EisError.OBJECT_ALREADY_EXIST.id,"添加权限关联失败，因为已存在该关联"));
			return ViewNames.partnerMessageView;
		}

		PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation(partner.getOwnerId());
		privilegeRoleRelation.setPrivilegeId(privilegeId);
		privilegeRoleRelation.setRoleId((int)roleId);
		int rs = partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
		logger.info("增加角色{}与权限:{}的关联结果:{}", roleId, privilegeId, rs);
		if(rs > 0) {
			cacheService.evict(CacheNames.cacheNameUser);
			map.put("message", EisMessage.success(OpResult.success.id,"成功关联角色和权限"));
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"添加权限关联失败，数据更新错误"));

		}
		return ViewNames.partnerMessageView;
	}
	
	
	@RequestMapping(value="/deleteMenuRelation", method=RequestMethod.POST)	
	@AllowJsonOutput
	@RequestPrivilege(object="partnerRole",operate="update")
	public String deleteMenuRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
		if(roleId <= 0) {
			logger.error("删除关联但未提交roleId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
			return ViewNames.partnerMessageView;

		}
		
		
		long menuId = ServletRequestUtils.getLongParameter(request, "menuId", 0);
		if(menuId <= 0) {
			logger.error("删除关联但未提交menuId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
			return ViewNames.partnerMessageView;

		}
		
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleId",roleId);
		criteria.put("menuId", menuId);
		int rs = partnerMenuRoleRelationService.deleteBy(criteria);

		if(rs > 0) {
			cacheService.evict(CacheNames.cacheNameUser);
			map.put("message", EisMessage.success(OpResult.success.getId(),"成功删除权限关联"));
		} else {
			map.put("message", EisMessage.error(OpResult.failed.getId(),"删除权限关联失败"));
		}
		return ViewNames.partnerMessageView;
	}
	
	
	
	/**
	 * 添加一个角色和菜单的关联
	 * 
	 */
	@RequestMapping(value="/addMenuRelation", method=RequestMethod.POST)	
	@AllowJsonOutput
	@RequestPrivilege(object="partnerRole",operate="update")
	public String addMenuRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
		
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		
		long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
		if(roleId <= 0) {
			logger.error("添加关联但未提交roleId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
			return ViewNames.partnerMessageView;

		}
		
		
		long menuId = ServletRequestUtils.getLongParameter(request, "menuId", 0);
		if(menuId <= 0) {
			logger.error("添加关联但未提交menuId");
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
			return ViewNames.partnerMessageView;

		}
		
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("roleIds",new long[] {roleId});
		criteria.put("menuId", menuId);
		int cnt = partnerMenuRoleRelationService.count(criteria);

		if(cnt > 0) {
			logger.warn("已存在roleId={},privilegeId={}的关联关系",roleId, menuId);
			map.put("message", EisMessage.error(EisError.OBJECT_ALREADY_EXIST.id,"添加权限关联失败，因为已存在该关联"));
			return ViewNames.partnerMessageView;
		}

		MenuRoleRelation privilegeRoleRelation = new MenuRoleRelation(partner.getOwnerId());
		privilegeRoleRelation.setMenuId(menuId);
		privilegeRoleRelation.setRoleId((int)roleId);
		int rs = partnerMenuRoleRelationService.insert(privilegeRoleRelation);
		logger.info("增加角色{}与菜单:{}的关联结果:{}", roleId, menuId, rs);
		if(rs > 0) {
			cacheService.evict(CacheNames.cacheNameUser);
			map.put("message", EisMessage.success(OpResult.success.id,"成功关联角色和菜单"));
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"添加菜单关联失败，数据更新错误"));

		}
		
		
		return ViewNames.partnerMessageView;
	}
	
	

}
