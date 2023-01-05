package com.maicard.boss.controller.common;

import java.util.List;
import java.util.Locale;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.ViewNames;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRoleRelation;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerMenuRoleRelationService;
import com.maicard.security.service.PartnerMenuService;
import com.maicard.security.service.PartnerRoleRelationService;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * 确定一个partner所能访问的菜单
 *
 * @author NetSnake
 * @date 2015年12月12日
 * 
 */
@Controller
@RequestMapping("/partnerMenuRoleRelation")
public class PartnerMenuRoleRelationController extends ValidateBaseController  {

	@Resource
	private AuthorizeService authorizeService;
	@Resource
	private CertifyService certifyService;
	@Resource
	private PartnerRoleRelationService partnerRoleRelationService;
	@Resource
	private PartnerMenuService partnerMenuService;
	@Resource
	private PartnerMenuRoleRelationService partnerMenuRoleRelationService;

	//为当前用户生成菜单
	@RequestMapping
	@AllowJsonOutput
	public String list(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
		User selfPartner = new User();
		boolean validateResult = loginValidate(request, response, map, selfPartner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}

		long currentUuid;
		if(selfPartner.getHeadUuid()>0){
			currentUuid=selfPartner.getHeadUuid();
		}else{
			currentUuid=selfPartner.getUuid();
		}
		CriteriaMap userRoleRelationCriteria = CriteriaMap.create(selfPartner.getOwnerId());
		userRoleRelationCriteria.put("uuid",currentUuid);
		userRoleRelationCriteria.put("currentStatus",BasicStatus.normal.id);
		List<UserRoleRelation> partnerRoleRelationList = partnerRoleRelationService.list(userRoleRelationCriteria);

		if(partnerRoleRelationList == null || partnerRoleRelationList.size() < 1){
			logger.warn("用户[" + selfPartner.getUuid() + "]没有任何岗位");
			return ViewNames.partnerMessageView;
		} 
		logger.info("用户[" + selfPartner.getUuid() + "]关联了" + partnerRoleRelationList.size() + "个岗位");
		int[] roleIds = new int[partnerRoleRelationList.size()];
		for(int i = 0 ; i < partnerRoleRelationList.size(); i++){
			roleIds[i] = partnerRoleRelationList.get(i).getRoleId();
		}
		

		CriteriaMap menuRoleRelationCriteria = CriteriaMap.create(selfPartner.getOwnerId());
		menuRoleRelationCriteria.put("currentStatus",BasicStatus.normal.id);
		menuRoleRelationCriteria.put("roleIds",roleIds);
		Locale locale = RequestContextUtils.getLocaleResolver(request).resolveLocale(request);
		menuRoleRelationCriteria.put("locale",locale);
		List<Menu> menuList = partnerMenuRoleRelationService.listInTree(menuRoleRelationCriteria);
		//logger.debug("使用locale=" + locale.getDisplayLanguage() + "，获取到所有菜单:" + JsonUtils.toStringFull(menuList));
		if(menuList != null){
			map.put("partnerMenuList", menuList);
		}
		return ViewNames.partnerMessageView;

	}




}
