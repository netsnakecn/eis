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
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.Operate;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.CacheService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.MenuRoleRelation;
import com.maicard.security.entity.User;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerMenuRoleRelationService;
import com.maicard.security.service.PartnerMenuService;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.PagingUtils;

@Controller
@RequestMapping("/partnerMenu")
public class PartnerMenuController extends ValidateBaseController {
	@Resource
	private CertifyService certifyService;
	@Resource
	private PartnerMenuService partnerMenuService;
	@Resource
	private AuthorizeService authorizeService; 
	@Resource
	private PartnerMenuRoleRelationService partnerMenuRoleRelationService;

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
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问菜单"));
			return ViewNames.partnerMessageView;
		}

		CriteriaMap criteria =  CriteriaMap.create(params);

		criteria.put("ownerId", partner.getOwnerId());
		int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);


		int totalRows = partnerMenuService.count(criteria);
		logger.info("一共  " + totalRows + " 行数据，每页显示 " + rows + " 行数据，当前是第 " + page + " 页。");


		List<Menu> partnerMenuList = partnerMenuService.listOnPage(criteria);


		map.put("operateCode", Operate.values());
		map.put("statusCodeList",BasicStatus.values());
		map.put("total", totalRows);
		map.put("rows",partnerMenuList);
		map.put("contentPaging", PagingUtils.generateContentPaging(totalRows, rows, page));

		map.put("operateMap",getOperateMap(partner, "partnerMenu"));

		return "common/partnerMenu/index";

	}

	@RequestMapping(value="/get" + "/{menuId}", method=RequestMethod.GET )	
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap map,	@PathVariable("menuId") Integer menuId) throws Exception {
		final String view = "common/partnerMenu/detail";
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问菜单"));
			return ViewNames.partnerMessageView;
		}

		if(menuId == 0){
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
			return ViewNames.partnerMessageView;			
		}
		Menu partnerMenu = partnerMenuService.select(menuId);
		if(partnerMenu == null){
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的菜单 id=" + menuId));
			return ViewNames.partnerMessageView;		
		}		
		if(partnerMenu.getOwnerId() != partner.getOwnerId()){
			logger.error("系统菜单[" + partnerMenu.getId() + "]的ownerId[" + partnerMenu.getOwnerId() + "]不属于当前ownerId:" + partner.getOwnerId());
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的菜单 id=" + menuId));
			return ViewNames.partnerMessageView;		
		}




		map.put("operateMap", this.getOperateMap(partner, "partnerMenu"));
		map.put("statusList", BasicStatus.values());
		map.put("partnerMenu", partnerMenu);
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
			map.put("message", EisMessage.error(OpResult.failed.id, "没有访问菜单"));
			return ViewNames.partnerMessageView;
		}

		long menuId = ServletRequestUtils.getLongParameter(request, "id", 0);

		if(menuId == 0){
			map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "未提供必须的参数menuId"));
			return ViewNames.partnerMessageView;			
		}
		Menu partnerMenu = partnerMenuService.select(menuId);
		if(partnerMenu == null){
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的菜单 id=" + menuId));
			return ViewNames.partnerMessageView;		
		}
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("menuId", menuId);
		int rs = partnerMenuRoleRelationService.deleteBy(criteria);
		logger.info("删除菜单:{}时，删除其菜单关联关系:{}条", menuId, rs);
		cacheService.evict(CacheNames.cacheNameUser);

		if (partnerMenuService.delete(menuId)==1) {
			map.put("message", EisMessage.success(OpResult.success.id, "菜单删除成功，同时删除" + rs + "条关联"));
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
		map.put("partnerMenu", new Menu());
		return "common/partnerMenu/create";
	}

	@RequestMapping(value="/create", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map, Menu partnerMenu) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		partnerMenu.setCurrentStatus(BasicStatus.normal.id);
		partnerMenu.setOwnerId(partner.getOwnerId());


		int rs = partnerMenuService.insert(partnerMenu);
		logger.info("增加菜单:{}的结果:{}", partnerMenu, rs);


		if(rs > 0) {

			map.put("message", EisMessage.success(OpResult.success.id,"成功添加菜单:" + partnerMenu.getId()));

		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"添加菜单关联失败，数据更新错误"));

		}
		return ViewNames.partnerMessageView;
	}



	@RequestMapping(value="/update", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map, Menu partnerMenu) throws Exception {
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		Menu old = partnerMenuService.select(partnerMenu.getId());
		if (old == null) {
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id,"找不到要修改的菜单"));
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
				criteria.put("menuId", old.getId());
				int deleteRs = partnerMenuRoleRelationService.deleteBy(criteria);
				int createRs = 0;
				logger.info("更新菜单关联角色，先删除所有角色跟菜单:{}的关联，结果:{}", old.getId(), deleteRs);
				for(int roleId : roleIdNumber) {
					if(roleId > 0) {
						MenuRoleRelation menuRoleRelation = new MenuRoleRelation(partner.getOwnerId());
						menuRoleRelation.setMenuId(partnerMenu.getId());
						menuRoleRelation.setRoleId(roleId);
						int rs = partnerMenuRoleRelationService.insert(menuRoleRelation);
						logger.info("更新菜单关联角色，增加角色:{}跟菜单:{}的关联，结果:{}", roleId, old.getId(), rs);
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

		if (StringUtils.isNotBlank(partnerMenu.getMenuName())) {
			old.setMenuName(partnerMenu.getMenuName());
		} 
		if (StringUtils.isNotBlank(partnerMenu.getMenuName())) {
			old.setMenuName(partnerMenu.getMenuName());
		} 
		if (partnerMenu.getCurrentStatus() > 0) {
			old.setCurrentStatus(partnerMenu.getCurrentStatus());
		}

		old.incrVersion();
		int rs = partnerMenuService.update(old);
		if(rs > 0) {
			map.put("message", EisMessage.success(OpResult.success.id,"修改成功"));
		} else {
			map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id,"修改失败"));

		}

		//map.put("message", message);
		return ViewNames.partnerMessageView;
	}
}
