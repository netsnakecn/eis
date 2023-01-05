package com.maicard.boss.controller.abs;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.security.entity.User;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerService;
import com.maicard.security.service.SecurityLevelService;
import com.maicard.site.iface.BoardProcessor;
import com.maicard.utils.SecurityLevelUtils;
import static com.maicard.site.constants.SiteConstants.DEFAULT_PAGE_SUFFIX;
import static com.maicard.site.constants.SiteConstants.PARTNER_LOGIN_URL;
@Controller
public class AbsIndexController extends ValidateBaseController{

	@Resource
	protected ApplicationContextService applicationContextService;
	@Resource
	protected AuthorizeService authorizeService;
	@Resource
	protected ConfigService configService;
	@Resource
	protected CertifyService certifyService;
	
	@Resource
	protected PartnerService partnerService;
	

	@Resource
	protected SecurityLevelService securityLevelService;

	@Value("${systemVersion}")
	protected String systemVersion;

	protected final String changePasswordUrl = "/user/update/userPassword" + DEFAULT_PAGE_SUFFIX;

	//protected static HashMap<String,Object> dashboardMap = new HashMap<String,Object>();

	protected int securityLevelId;
	protected SecurityLevel securityLevel;

	@PostConstruct
	public void init(){
		securityLevelId = SecurityLevelUtils.getSecurityLevel();
		securityLevel = securityLevelService.select(securityLevelId);
	}



	@RequestMapping(method=RequestMethod.GET)
	@IgnorePrivilegeCheck
	public String index(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {		
		User partner =certifyService.getLoginedUser(request,response,UserTypes.partner.id);
		if(partner == null){
			//为防止转发带上参数，不再使用Spring的redirect而采用response的redirect重定向
			response.sendRedirect(PARTNER_LOGIN_URL);
			return null;
			//return "redirect:/user/login";
		} 
		//logger.debug("本地消息测试:" + applicationContextService.getLocaleMessage("Status.100001"));
		logger.debug("用户[" + partner.getUsername() + "]进入合作伙伴系统.");
		//检查密码是否过期
		boolean  passwordNeedChange = certifyService.passwordNeedChange(partner);
		if(passwordNeedChange){
			logger.debug("当前用户的密码需要进行更改,跳转到:" + changePasswordUrl);
			response.sendRedirect(changePasswordUrl);
			return null;
		}

		writeDashboard(map, partner);


		writeVersion(map);

		writeSecurityLevel(map);

		writePerformanceRate(map);


		String index = configService.getValue("boardFile", partner.getOwnerId());
		if(StringUtils.isBlank(index)) {
			index = "index_normal";
		}

		return "biz/index/" + index;
	}


	protected void writeVersion(ModelMap map) {
		map.put("systemVersion", systemVersion);		
	}

	protected void writeSecurityLevel(ModelMap map) {
		map.put("securityLevel", securityLevel);
		map.put("securityLevelId", securityLevelId);		
	}

	protected void writePerformanceRate(ModelMap map) {
		map.put("performanceRate", applicationContextService.getPerformanceRate());
	}





	protected void writeDashboard(ModelMap map, User partner) {		
		String boardProcessorName = configService.getValue("boardProcessor", partner.getOwnerId());
		if(StringUtils.isBlank(boardProcessorName)) {
			logger.warn("系统未配置ownerId={}的boardProcessor",partner.getOwnerId());
			return;
		}
		BoardProcessor bp = applicationContextService.getBeanGeneric(boardProcessorName);
		if(bp == null) {
			logger.warn("系统中没有名为{}的boardProcessor",boardProcessorName);
			return;
	
		}
		
		bp.writeBoard(map, partner);



	}




}
