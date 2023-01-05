package com.maicard.boss.controller.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.Operate;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.CacheService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.PrivilegeRoleRelation;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerPrivilegeRoleRelationService;
import com.maicard.security.service.PartnerPrivilegeService;
import com.maicard.security.service.PartnerRoleService;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.PagingUtils;

@Controller
@RequestMapping("/partnerPrivilege")
public class PartnerPrivilegeController extends ValidateBaseController {
	@Resource
	private CertifyService certifyService;
	@Resource
	private PartnerPrivilegeService partnerPrivilegeService;
	@Resource
	private AuthorizeService authorizeService;
	@Resource
	private PartnerRoleService partnerRoleService;
	@Resource
	private PartnerPrivilegeRoleRelationService partnerPrivilegeRoleRelationService;

	@Resource
	private CacheService cacheService;


	@RequestMapping(method=RequestMethod.GET)	
	public String list(HttpServletRequest request, HttpServletResponse response, ModelMap map,  @RequestParam Map<String, Object> params) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问权限"));
			return ViewNames.partnerMessageView;
		}

		CriteriaMap criteria =  CriteriaMap.create(params);

		criteria.put("ownerId", partner.getOwnerId());
		int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);


		int totalRows = partnerPrivilegeService.count(criteria);
		logger.info("一共  " + totalRows + " 行数据，每页显示 " + rows + " 行数据，当前是第 " + page + " 页。");


		List<Privilege> partnerPrivilegeList = partnerPrivilegeService.listOnPage(criteria);


		map.put("operateCode", Operate.values());
		map.put("objectTypes", ObjectType.values());
		map.put("statusCodeList",BasicStatus.values());
		map.put("total", totalRows);
		map.put("rows",partnerPrivilegeList);
		map.put("contentPaging", PagingUtils.generateContentPaging(totalRows, rows, page));

		map.put("operateMap",getOperateMap(partner, "partnerPrivilege"));

		return "common/partnerPrivilege/index";

	}

	@RequestMapping(value="/get" + "/{privilegeId}", method=RequestMethod.GET )	
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap map,	@PathVariable("privilegeId") Integer privilegeId) throws Exception {
		final String view = "common/partnerPrivilege/detail";
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问权限"));
			return ViewNames.partnerMessageView;
		}

		if(privilegeId == 0){
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
			return ViewNames.partnerMessageView;			
		}
		Privilege partnerPrivilege = partnerPrivilegeService.select(privilegeId);
		if(partnerPrivilege == null){
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的权限 id=" + privilegeId));
			return ViewNames.partnerMessageView;		
		}		
		if(partnerPrivilege.getOwnerId() != partner.getOwnerId()){
			logger.error("系统权限[" + partnerPrivilege.getId() + "]的ownerId[" + partnerPrivilege.getOwnerId() + "]不属于当前ownerId:" + partner.getOwnerId());
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的权限 id=" + privilegeId));
			return ViewNames.partnerMessageView;		
		}
		map.put("operateCode", Operate.values());
		map.put("objectTypes", ObjectType.values());

		//获得所有角色列表
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		List<Role> allRoleList = partnerRoleService.list(criteria);


		//获得角色列表
		criteria.init();
		criteria.put("privilegeId",privilegeId);
		List<PrivilegeRoleRelation> list = partnerPrivilegeRoleRelationService.list(criteria);
		List<Role> roles = new ArrayList<>(); 

		for(Role role : allRoleList) {
			Role r2 = null;
			for (PrivilegeRoleRelation privilegeRoleRelation : list) {
				if(role.getId() == privilegeRoleRelation.getRoleId()) {
					r2 = role.clone();
					r2.setCurrentStatus(BasicStatus.defaulted.id);
					break;						
				}
			}
			if(r2 == null) {
				r2 = role;
			}
			roles.add(r2);
		}


		map.put("operateMap", this.getOperateMap(partner, "partnerPrivilege"));
		map.put("roleList", roles);
		map.put("statusList", BasicStatus.values());
		map.put("partnerPrivilege", partnerPrivilege);
		return view;
	}


	@RequestMapping(value="/delete", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map
			) throws Exception {
		final String view = ViewNames.partnerMessageView;
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问权限"));
			return ViewNames.partnerMessageView;
		}

		long privilegeId = ServletRequestUtils.getLongParameter(request, "id", 0);

		if(privilegeId == 0){
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
			return ViewNames.partnerMessageView;			
		}
		Privilege partnerPrivilege = partnerPrivilegeService.select(privilegeId);
		if(partnerPrivilege == null){
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的权限 id=" + privilegeId));
			return ViewNames.partnerMessageView;		
		}
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("privilegeId", privilegeId);
		int rs = partnerPrivilegeRoleRelationService.deleteBy(criteria);
		logger.info("删除权限:{}时，删除其权限关联关系:{}条", privilegeId, rs);
		cacheService.evict(CacheNames.cacheNameUser);

		if (partnerPrivilegeService.delete(privilegeId)==1) {
			map.put("message", EisMessage.success(OpResult.success.id, "权限删除成功，同时删除" + rs + "条关联"));
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id, "操作失败"));
		}
		return view;
	}

	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String getCreate(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}


		map.put("statusList", BasicStatus.values());
		map.put("operateCode", Operate.values());
		map.put("objectTypes", ObjectType.values());
		map.put("partnerPrivilege", new Privilege());
		return "common/partnerPrivilege/create";
	}

	@RequestMapping(value="/create", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map, Privilege partnerPrivilege) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		partnerPrivilege.setCurrentStatus(BasicStatus.normal.id);
		partnerPrivilege.setOwnerId(partner.getOwnerId());

		Assert.isTrue(StringUtils.isNotBlank(partnerPrivilege.getObjectTypeCode()),"新增权限的objectTypeCode不能为空");
		Assert.isTrue(StringUtils.isNotBlank(partnerPrivilege.getOperateCode()),"新增权限的operateCode不能为空");



		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("objectTypeCode",partnerPrivilege.getObjectTypeCode());
		criteria.put("operateCode", partnerPrivilege.getOperateCode());
		int cnt = partnerPrivilegeService.count(criteria);

		if(cnt > 0) {
			logger.warn("已存在objectTypeCode={},operateCode={}的权限",partnerPrivilege.getObjectTypeCode(), partnerPrivilege.getOperateCode());
			map.put("message", EisMessage.error(EisError.OBJECT_ALREADY_EXIST.id,"添加权限失败，因为已存在类似权限"));
			return ViewNames.partnerMessageView;
		}





		int rs = partnerPrivilegeService.insert(partnerPrivilege);
		logger.info("增加权限:{}的结果:{}", partnerPrivilege, rs);


		if(rs > 0) {
			long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);


			if(roleId > 0) {
				PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation(partner.getOwnerId());
				privilegeRoleRelation.setPrivilegeId(partnerPrivilege.getId());
				privilegeRoleRelation.setRoleId((int)roleId);
				rs = partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
				logger.info("添加权限的同时，为角色:{}增加该权限的关联",partnerPrivilege.getId(), roleId, rs);	
				if(rs > 0) {
					map.put("message", EisMessage.success(OpResult.success.id,"成功关联权限:" + partnerPrivilege.getId() + ",和角色关联"));
				} else {
					map.put("message", EisMessage.success(OpResult.success.id,"成功关联权限，但未能增加角色关联"));
				}

			} else {
				map.put("message", EisMessage.success(OpResult.success.id,"成功添加权限:" + partnerPrivilege.getId()));
			}
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"添加权限关联失败，数据更新错误"));

		}
		return ViewNames.partnerMessageView;
	}



	@RequestMapping(value="/update", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map, Privilege partnerPrivilege) throws Exception {
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		Privilege old = partnerPrivilegeService.select(partnerPrivilege.getId());
		if (old == null) {
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id,"找不到要修改的权限"));
			return ViewNames.partnerMessageView;
		}


		String roleIdStr = ServletRequestUtils.getStringParameter(request, "roleId");
		if(StringUtils.isNotBlank(roleIdStr)) {
			String[] roleIds = roleIdStr.split(",");
			List<Integer> roleIdNumber = new ArrayList<Integer>();
			for(String roleIdS : roleIds) {
				int roleId = NumericUtils.parseInt(roleIdS);
				if(roleId > 0) {
					roleIdNumber.add(roleId);
				}

			}
			if(roleIdNumber.size() > 0) {
				CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
				criteria.put("privilegeId", old.getId());
				int deleteRs = partnerPrivilegeRoleRelationService.deleteBy(criteria);
				int createRs = 0;
				logger.info("更新权限关联角色，先删除所有角色跟权限:{}的关联，结果:{}", old.getId(), deleteRs);
				for(int roleId : roleIdNumber) {
					if(roleId > 0) {
						PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation(partner.getOwnerId());
						privilegeRoleRelation.setPrivilegeId(partnerPrivilege.getId());
						privilegeRoleRelation.setRoleId(roleId);
						int rs = partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
						logger.info("更新权限关联角色，增加角色:{}跟权限:{}的关联，结果:{}", roleId, old.getId(), rs);
						if(rs > 0) {
							createRs++;
						}
					}
				}
				map.put("message", EisMessage.success(OpResult.success.id,"修改成功，删除了" + deleteRs + "个旧关联，新增了" + createRs + "个关联"));

				cacheService.evict(CacheNames.cacheNameUser);
				return ViewNames.partnerMessageView;
			} else {
				logger.error("错误的roleId参数:{}", roleIdStr);

				map.put("message", EisMessage.error(EisError.PARAMETER_ERROR.id,"错误的roleId参数"));
				return ViewNames.partnerMessageView;
				
			}
		}

		if (StringUtils.isNotBlank(partnerPrivilege.getObjectTypeCode())) {
			old.setObjectTypeCode(partnerPrivilege.getObjectTypeCode());
		}
		if (StringUtils.isNotBlank(partnerPrivilege.getPrivilegeDesc())) {
			old.setPrivilegeDesc(partnerPrivilege.getPrivilegeDesc());
		}
		if (StringUtils.isNotBlank(partnerPrivilege.getPrivilegeName())) {
			old.setPrivilegeName(partnerPrivilege.getPrivilegeName());
		}
		if (StringUtils.isNotBlank(partnerPrivilege.getOperateCode())) {
			old.setOperateCode(partnerPrivilege.getOperateCode());
		}
		if (StringUtils.isNotBlank(partnerPrivilege.getObjectList())) {
			old.setObjectList(partnerPrivilege.getObjectList());
		}
		if (StringUtils.isNotBlank(partnerPrivilege.getObjectAttributePattern())) {
			old.setObjectAttributePattern(partnerPrivilege.getObjectAttributePattern());
		}
		if (partnerPrivilege.getCurrentStatus() > 0) {
			old.setCurrentStatus(partnerPrivilege.getCurrentStatus());
		}

		old.incrVersion();
		int rs = partnerPrivilegeService.update(old);
		if(rs > 0) {
			map.put("message", EisMessage.success(OpResult.success.id,"修改成功"));
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"修改失败"));

		}

		//map.put("message", message);
		return ViewNames.partnerMessageView;
	}
}
