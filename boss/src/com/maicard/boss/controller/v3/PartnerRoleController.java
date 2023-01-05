package com.maicard.boss.controller.v3;

import com.maicard.base.CriteriaMap;
import com.maicard.site.utils.ResponseUtil;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.*;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.CacheService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.annotation.RequestPrivilege;
import com.maicard.security.constants.RoleTypes;
import com.maicard.security.entity.*;
import com.maicard.security.service.*;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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


    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String list(HttpServletRequest request, HttpServletResponse response, ModelMap map, @RequestParam Map<String, Object> params) throws Exception {


        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
        String roleLevelStr = ServletRequestUtils.getStringParameter(request, "roleLevel", null);
        if (StringUtils.isBlank(roleLevelStr) && !isPlatformGenericPartner) {
            logger.error("非内部用户列出角色但未提交roleLevel");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleLevel"));
            return ViewNames.partnerMessageView;

        }

        CriteriaMap criteria = CriteriaMap.create(params);
        sort(criteria, Role.class);

//		if(!isPlatformGenericPartner) {
        //不是内部用户，检查其能选择的角色
        criteria.putArray("roleLevels", partnerRoleService.getValidLevel(partner, roleLevelStr));
        criteria.remove("roleLevel");
        //	}
        int totalRows = partnerRoleService.count(criteria);
        List<Role> partnerRoleList = partnerRoleService.list(criteria);

        map.put("message", EisMessage.success());
        map.put("total", totalRows);
        map.put("rows", partnerRoleList);
        map.put("operateMap", getOperateMap(partner, "partnerRole"));


        return VIEW;

    }

	/*@RequestMapping(value="/get" + "/{roleId}", method=RequestMethod.GET )
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
	}*/

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         @RequestParam("id") long id) throws Exception {
        if (id <= 0) {
            throw new EisException(EisError.REQUIRED_PARAMETER.id, "请求中找不到必须的参数[idList]");
        }
        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }


        Role partnerRole = partnerRoleService.select(id);
        if (partnerRole == null) {
            logger.warn("找不到要删除的PartnerRole，ID=" + id);
            return ResponseUtil.error(map, EisError.OBJECT_IS_NULL.id, "找不到要删除的角色:" + id);
        }

        if (partnerRole.getOwnerId() != partner.getOwnerId()) {
            logger.warn("要删除的PartnerRole，ownerId[" + partnerRole.getOwnerId() + "]与系统会话中的ownerId不一致:" + id);
            return ResponseUtil.error(map, EisError.OBJECT_IS_NULL.id, "找不到要删除的角色:" + id);
        }

        CriteriaMap criteriaMap = CriteriaMap.create().putArray("roleIds",id);
        int haveRelation = partnerRoleRelationService.count(criteriaMap);
        if(haveRelation > 0){
            logger.error("角色还有:" + haveRelation + "个对应partner，不能删除");
            return ResponseUtil.error(map,EisError.RELATION_DATA_CONFILECT.id, "还有关联此角色的用户");
        }
        int rs = partnerRoleService.delete(id);
        if (rs == OpResult.success.id) {
            return ResponseUtil.success(map);
        } else {
            return ResponseUtil.error(map, EisError.DATA_UPDATE_FAIL.id, "数据更新失败");
        }

    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @AllowJsonOutput
    public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         @RequestBody Role partnerRole) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }
        partnerRole.setOwnerId(partner.getOwnerId());
        if (partnerRole.getRoleType() == 0) {
            partnerRole.setRoleType(RoleTypes.ROLE_TYPE_USER.id);
        }
        //获取额外的权限关系
        try {
            int rs = partnerRoleService.insert(partnerRole);
            if (rs != 1) {
                map.put("message", EisMessage.error(EisError.DATA_ERROR.id, "无法添加"));
                return ViewNames.partnerMessageView;
            }
            if (partnerRole.getRoleType() == RoleTypes.ROLE_TYPE_USER.id) {
                for (int i = 0; i < partnerRole.getRelatedPrivilegeList().size(); i++) {
                    PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation();
                    privilegeRoleRelation.setOwnerId(partner.getOwnerId());
                    privilegeRoleRelation.setPrivilegeId(partnerRole.getRelatedPrivilegeList().get(i).getId());
                    privilegeRoleRelation.setRoleId(partnerRole.getId());
                    partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
                }

            }
        } catch (Exception e) {
            throw new EisException(EisError.DATA_UPDATE_FAIL.id, "数据操作失败" + e.getMessage());
        }
        map.put("message", EisMessage.success());
        return ViewNames.partnerMessageView;

    }


    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @AllowJsonOutput
    public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         @RequestBody Role partnerRole) throws Exception {
        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }
        Assert.isTrue(partnerRole.getId() > 0, "修改对象ID必须大于0");
        Role _oldPartnerRole = partnerRoleService.select(partnerRole.getId());
        if (_oldPartnerRole == null) {
            logger.error("找不到要修改的PartnerRole，ID=" + partnerRole.getId());
            return ViewNames.partnerMessageView;
        }
        if (_oldPartnerRole.getOwnerId() != partner.getOwnerId()) {
            logger.warn("要修改的PartnerRole，ownerId[" + _oldPartnerRole.getOwnerId() + "]与系统会话中的ownerId不一致:" + partner.getOwnerId());
            return ViewNames.partnerMessageView;
        }
        if (partnerRole.getRoleType() == 0) {
            partnerRole.setRoleType(_oldPartnerRole.getRoleType());
        }
        if (partnerRole.getRoleType() == 0) {
            partnerRole.setRoleType(RoleTypes.ROLE_TYPE_USER.id);
        }
        partnerRole.setOwnerId(_oldPartnerRole.getOwnerId());
        //获取额外的权限关系
        try {
            int rs = partnerRoleService.update(partnerRole);
            if (rs != 1) {
                map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id, "无法更新"));
                return ViewNames.partnerMessageView;
            }
            if (partnerRole.getRoleType() == RoleTypes.ROLE_TYPE_USER.id) {
                partnerPrivilegeRoleRelationService.deleteBy(CriteriaMap.create().put("roleId", partnerRole.getId()));
                for (int i = 0; i < partnerRole.getRelatedPrivilegeList().size(); i++) {
                    PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation();
                    privilegeRoleRelation.setOwnerId(partner.getOwnerId());
                    privilegeRoleRelation.setPrivilegeId(partnerRole.getRelatedPrivilegeList().get(i).getId());
                    privilegeRoleRelation.setRoleId(partnerRole.getId());
                    partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
                }

            }
        } catch (Exception e) {
            throw new EisException(EisError.DATA_UPDATE_FAIL.id, "数据操作失败" + e.getMessage());
        }
        map.put("message", EisMessage.success());
        return ViewNames.partnerMessageView;

    }

    @RequestMapping(value = "/deletePrivilegeRelation", method = RequestMethod.POST)
    @AllowJsonOutput
    @RequestPrivilege(object = "partnerRole", operate = "update")
    public String deletePrivilegeRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
        if (roleId <= 0) {
            logger.error("删除关联但未提交roleId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
            return ViewNames.partnerMessageView;

        }


        long privilegeId = ServletRequestUtils.getLongParameter(request, "privilegeId", 0);
        if (privilegeId <= 0) {
            logger.error("删除关联但未提交privilegeId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
            return ViewNames.partnerMessageView;

        }

        CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
        criteria.put("roleId", roleId);
        criteria.put("privilegeId", privilegeId);
        int rs = partnerPrivilegeRoleRelationService.deleteBy(criteria);

        if (rs > 0) {
            cacheService.evict(CacheNames.cacheNameUser);
            map.put("message", EisMessage.success(OpResult.success.getId(), "成功删除权限关联"));
        } else {
            map.put("message", EisMessage.error(OpResult.failed.getId(), "删除权限关联失败"));
        }
        return ViewNames.partnerMessageView;
    }


    /**
     * 添加一个角色和权限的关联
     */
    @RequestMapping(value = "/addPrivilegeRelation", method = RequestMethod.POST)
    @AllowJsonOutput
    @RequestPrivilege(object = "partnerRole", operate = "update")
    public String addPrivilegeRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
        if (roleId <= 0) {
            logger.error("添加关联但未提交roleId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
            return ViewNames.partnerMessageView;

        }


        long privilegeId = ServletRequestUtils.getLongParameter(request, "privilegeId", 0);
        if (privilegeId <= 0) {
            logger.error("添加关联但未提交privilegeId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数privilegeId"));
            return ViewNames.partnerMessageView;

        }

        CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
        criteria.put("roleId", roleId);
        criteria.put("privilegeId", privilegeId);
        int cnt = partnerPrivilegeRoleRelationService.count(criteria);

        if (cnt > 0) {
            logger.warn("已存在roleId={},privilegeId={}的关联关系", roleId, privilegeId);
            map.put("message", EisMessage.error(EisError.OBJECT_ALREADY_EXIST.id, "添加权限关联失败，因为已存在该关联"));
            return ViewNames.partnerMessageView;
        }

        PrivilegeRoleRelation privilegeRoleRelation = new PrivilegeRoleRelation(partner.getOwnerId());
        privilegeRoleRelation.setPrivilegeId(privilegeId);
        privilegeRoleRelation.setRoleId((int) roleId);
        int rs = partnerPrivilegeRoleRelationService.insert(privilegeRoleRelation);
        logger.info("增加角色{}与权限:{}的关联结果:{}", roleId, privilegeId, rs);
        if (rs > 0) {
            cacheService.evict(CacheNames.cacheNameUser);
            map.put("message", EisMessage.success(OpResult.success.id, "成功关联角色和权限"));
        } else {
            map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id, "添加权限关联失败，数据更新错误"));

        }
        return ViewNames.partnerMessageView;
    }


    @RequestMapping(value = "/deleteMenuRelation", method = RequestMethod.POST)
    @AllowJsonOutput
    @RequestPrivilege(object = "partnerRole", operate = "update")
    public String deleteMenuRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
        if (roleId <= 0) {
            logger.error("删除关联但未提交roleId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
            return ViewNames.partnerMessageView;

        }


        long menuId = ServletRequestUtils.getLongParameter(request, "menuId", 0);
        if (menuId <= 0) {
            logger.error("删除关联但未提交menuId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
            return ViewNames.partnerMessageView;

        }

        CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
        criteria.put("roleId", roleId);
        criteria.put("menuId", menuId);
        int rs = partnerMenuRoleRelationService.deleteBy(criteria);

        if (rs > 0) {
            cacheService.evict(CacheNames.cacheNameUser);
            map.put("message", EisMessage.success(OpResult.success.getId(), "成功删除权限关联"));
        } else {
            map.put("message", EisMessage.error(OpResult.failed.getId(), "删除权限关联失败"));
        }
        return ViewNames.partnerMessageView;
    }


    /**
     * 添加一个角色和菜单的关联
     */
    @RequestMapping(value = "/addMenuRelation", method = RequestMethod.POST)
    @AllowJsonOutput
    @RequestPrivilege(object = "partnerRole", operate = "update")
    public String addMenuRelation(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        long roleId = ServletRequestUtils.getLongParameter(request, "roleId", 0);
        if (roleId <= 0) {
            logger.error("添加关联但未提交roleId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数roleId"));
            return ViewNames.partnerMessageView;

        }


        long menuId = ServletRequestUtils.getLongParameter(request, "menuId", 0);
        if (menuId <= 0) {
            logger.error("添加关联但未提交menuId");
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
            return ViewNames.partnerMessageView;

        }

        CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
        criteria.put("roleIds", new long[]{roleId});
        criteria.put("menuId", menuId);
        int cnt = partnerMenuRoleRelationService.count(criteria);

        if (cnt > 0) {
            logger.warn("已存在roleId={},privilegeId={}的关联关系", roleId, menuId);
            map.put("message", EisMessage.error(EisError.OBJECT_ALREADY_EXIST.id, "添加权限关联失败，因为已存在该关联"));
            return ViewNames.partnerMessageView;
        }

        MenuRoleRelation privilegeRoleRelation = new MenuRoleRelation(partner.getOwnerId());
        privilegeRoleRelation.setMenuId(menuId);
        privilegeRoleRelation.setRoleId((int) roleId);
        int rs = partnerMenuRoleRelationService.insert(privilegeRoleRelation);
        logger.info("增加角色{}与菜单:{}的关联结果:{}", roleId, menuId, rs);
        if (rs > 0) {
            cacheService.evict(CacheNames.cacheNameUser);
            map.put("message", EisMessage.success(OpResult.success.id, "成功关联角色和菜单"));
        } else {
            map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id, "添加菜单关联失败，数据更新错误"));

        }


        return ViewNames.partnerMessageView;
    }


}
