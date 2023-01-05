package com.maicard.site.aspect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.base.BaseService;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.security.service.PartnerService;
import com.maicard.site.constants.SiteConstants;
import com.maicard.site.service.SiteDomainRelationService;
import com.maicard.site.utils.HtmlTagTrim;
import com.maicard.utils.CookieUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ui.ModelMap;

import static com.maicard.site.constants.SiteConstants.DEFAULT_PAGE_SUFFIX;

/**
 * @version 2
 * NetSnake, 2016.9.28<br>
 * 修改了切入点的配置<br>
 * execution(* com.maicard..*.front.controller..*.*(..))<br>
 * 改为<br>
 * execution(* com.maicard..*.controller..*.*(..))<br>
 * 
 * @version 1
 * 用于对前台控制器的一般通用数据进行统一注入<br>
 * 第一优先级织入
 * 
 * @author NetSnake
 * @date 2013-2-27
 */
@Aspect
@Order(1)
public class SiteGeneralDataInjectAspect extends BaseService{

	@Resource
	private ConfigService configService;  
	@Resource
	private PartnerService partnerService;
	@Resource
	private SiteDomainRelationService siteDomainRelationService;

	@Resource
	private ApplicationContextService applicationContextService;

	//@Resource
	//private SiteThemeRelationService siteThemeRelationService;

	private int serverId;
	private String systemCode;
	private String defaultNodeProcessor;
	private boolean validateUserIp;

	static String GLOBAL_LOCALE = null;
	boolean performanceCheck = true;

	private final String year = new SimpleDateFormat("yyyy").format(new Date());

	private final String[] ignoreRedirectUri = new String[]{"/weixinUser/",  "/content/user/login" + DEFAULT_PAGE_SUFFIX,"/content/user/register" + DEFAULT_PAGE_SUFFIX,  "/captcha" + DEFAULT_PAGE_SUFFIX};

	//用于存放前端配置的缓存
	//private static Map<Long, FrontSetting> frontSettingCache = new LinkedHashMap<Long, FrontSetting>();
	private static Map<Long, FrontSetting> frontSettingCache = new ConcurrentHashMap<Long, FrontSetting>();


	@PostConstruct
	public void init(){
		systemCode = configService.getSystemCode();
		defaultNodeProcessor = configService.getValue(DataName.siteDefaultNodeProcessor.toString(),0);
		//siteStaticize = configService.getBooleanValue(DataName.siteStaticize.toString(),0);
		serverId = configService.getServerId();
		validateUserIp = configService.getBooleanValue(DataName.validateUserIp.toString(),0);
		GLOBAL_LOCALE = configService.getValue(DataName.GLOBAL_LOCALE.name(),0);
	}


	//@Around("execution(* com.maicard..*.*(..))" 
	@Around("execution(* com..*.controller..*.*(..))" 
	+ "&& @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

		long startTs = 0;
		if(performanceCheck){
			startTs = new Date().getTime();
		}

		ModelMap map = null;
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		try{
			map = (ModelMap)joinPoint.getArgs()[2];
			request  = (HttpServletRequest)joinPoint.getArgs()[0];
		}catch(Exception e){}
		if(map == null || request == null){
			throw new EisException(EisError.SYSTEM_DATA_ERROR.id,"系统规范异常");
		}
		try{
			response  = (HttpServletResponse)joinPoint.getArgs()[1];
		}catch(Exception e){}
		if(response != null){
			response.setHeader("eisServerId", systemCode + "-" + serverId);
		} else {
			logger.error("找不到系统response");
			return null;
		}

		logger.debug("开始前端一般性数据注入:" + joinPoint.getTarget().getClass().getName() + "/" + joinPoint.getSignature().getName() + ",URL:" + request.getRequestURI() + "请求编码:" + request.getCharacterEncoding());
		if(validateUserIp){
			boolean userIpIsForbidden = userIpIsForbidden(request);
			if(logger.isDebugEnabled()){
				logger.debug("检查用户IP是否被屏蔽，检查结果:" + userIpIsForbidden);
			}
			if(userIpIsForbidden){
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return null;
			}
		}
		String hostName = request.getServerName();
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(hostName);
		if(siteDomainRelation == null){
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		map.put("ownerId", siteDomainRelation.getOwnerId());
		setSiteSpecialSetting(request, map, siteDomainRelation.getOwnerId());

		//获取当前URL的主机前缀
		/*String hostCode = hostName.split("\\.")[0];
		CriteriaMap siteThemeRelationCriteria = new CriteriaMap();
		siteThemeRelationCriteria.put("hostCode",hostCode);
		siteThemeRelationCriteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		
		SiteThemeRelation siteThemeRelation = siteThemeRelationService.select(siteThemeRelationCriteria);
		if(siteThemeRelation != null){
			map.put("theme", siteThemeRelation.getThemeCode());
			if(StringUtils.isNotBlank(siteThemeRelation.getSiteName())){
				//使用商户的名字代替系统名称
				map.put("systemName", siteThemeRelation.getSiteName());
			}
			map.put(DataName.sitePartnerId.toString(), siteThemeRelation.getUuid());
			logger.debug("为当前主机代码[" + hostCode + ",主机名:" + hostName + "]找到的主题配置是:" + siteThemeRelation.getSiteThemeRelationId() + ",合作方是:" + siteThemeRelation.getUuid() + ", 放入主题代码:" + siteThemeRelation.getThemeCode() + ",替换系统名称:" + siteThemeRelation.getSiteName());
		}*/


		if(defaultNodeProcessor == null){
			logger.warn("系统没有defaultNodeProcessor配置");
			//response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			//return null;
		}

		String uri = request.getRequestURI();
		if(!uri.endsWith(DEFAULT_PAGE_SUFFIX) && !uri.equals("/") ){
			logger.debug("当前请求[" + uri + "]不是以" + DEFAULT_PAGE_SUFFIX + "结尾，也不是/, 不写入重定向数据");
		} else {
			boolean ignore = false;
			for(String ignoreData : ignoreRedirectUri){
				if(uri.indexOf(ignoreData) > -1){
					logger.debug("当前请求[" + uri + "]与忽略数据[" + ignoreData + "]匹配，不写入重定向数据");
					ignore = true;
					break;
				}
			}
			if(!ignore){
				if (request.getQueryString()!=null){
					uri=uri+"?"+request.getQueryString();
				}
				logger.debug("把当前请求[" + uri + "]写入重定向Cookie");
				CookieUtils.addCookie(request, response, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_f", java.net.URLEncoder.encode(uri,"UTF-8"), Constants.COOKIE_MAX_TTL, siteDomainRelation.getCookieDomain());
			}
		}


		String siteCode = siteDomainRelation.getSiteCode();
		logger.debug("根据主机名[" + request.getServerName() + "]确定当前系统使用站点代码:" + siteCode + ",ownerId=" + siteDomainRelation.getOwnerId());
		map.put("siteCode", siteCode);

		//检查是否有邀请码参数
		String inviteCode = request.getParameter("i");
		if(StringUtils.isBlank(inviteCode)){
			logger.debug("当前请求中没有i参数，即没有邀请码");

		} else {
			logger.debug("当前请求有i参数=" + inviteCode + ",写入Cookie");
			CookieUtils.addCookie(request, response, "inviteCode", inviteCode, Constants.COOKIE_MAX_TTL, null);
		}

		Object o = joinPoint.proceed();
		if(logger.isDebugEnabled() && performanceCheck){
			long time = new Date().getTime() - startTs;
			logger.debug("性能检查::执行时间:" + time + "毫秒");
		}
		
		map.remove("systemCode");
		map.remove("ownerId");

		//此处执行翻译
		/*if(map.containsKey("message")){
			Object m = map.get("message");
			if(m instanceof EisMessage){
				EisMessage eisMessage = (EisMessage) m;
				EisMessage translated = translate(eisMessage);
				map.put("message", translated);
			}
		} else {
			logger.debug("本次返回没有message");
		}*/
		return o;

	}

	private EisMessage translate(EisMessage eisMessage) {

		Locale locale = null;
		if(GLOBAL_LOCALE != null){
			locale = Locale.forLanguageTag(GLOBAL_LOCALE);
		}

		if(locale == null){
			locale = LocaleContextHolder.getLocale();
		}

		if (eisMessage == null ) {
			return eisMessage;
		}

		//对于包含网址信息的不处理
		if(eisMessage.message != null && eisMessage.message.indexOf("://") > 0){
			return eisMessage;
		}

		MessageSource	msgBean = applicationContextService.getBeanGeneric(MessageSource.class);
		if(msgBean == null){
			logger.error("系统没有注册" + MessageSource.class + "的bean");
			eisMessage.message = "";
			return eisMessage;
		}


		String transMsg = null;
		if(StringUtils.isBlank(eisMessage.message)){
			//找出Status的翻译
			transMsg = msgBean.getMessage("Status." + eisMessage.code, null, locale);
		} else {
			transMsg = msgBean.getMessage(eisMessage.message, null, locale);
		}

		logger.debug("当前地区是:" + locale + ",全局设定区域是:" + GLOBAL_LOCALE + ",把消息:" + eisMessage.code + "=>" + eisMessage.message + "翻译为:" + transMsg);
		eisMessage.setMessage(transMsg);
		return eisMessage;
	}


	private void setSiteSpecialSetting(HttpServletRequest request, ModelMap map, long ownerId) throws Exception{

		//map.put(DataName.siteStaticize.toString(), siteStaticize);

		FrontSetting frontSetting = getFrontSetting(ownerId);

		if(frontSetting.theme != null) {
			map.put("theme", frontSetting.theme);
		}
/*
		if(frontSetting.moneyName != null) {
			map.put("moneyName", frontSetting.moneyName);
		}

		if(frontSetting.coinName != null) {
			map.put("coinName", frontSetting.coinName);
		}
		if(frontSetting.pointName != null) {
			map.put("pointName", frontSetting.pointName);
		}
		if(frontSetting.scoreName != null) {
			map.put("scoreName",frontSetting.scoreName);
		}
		String returnUrl = request.getRequestURL().toString();
		if(request.getQueryString() != null){
			returnUrl += "?" + request.getQueryString();
		}
		map.put("frontLoginUrl", frontSetting.loginUrl + "?returnUrl=" + returnUrl);
		map.put("commonFooter", frontSetting.commonFooter);
*/
		map.put("systemName", frontSetting.systemName);


		map.put("systemCode", systemCode);



		/*String theme = null;
		if(siteUseMultiDomain){
			UserCriteria partnerCriteria = new UserCriteria();
			//partnerCriteria.setCurrentStatus(SecurityStandard.UserStatus.normal.getId());
			partnerCriteria.setUserTypeId(SecurityStandard.UserTypes.partner.getId());
			partnerCriteria.setUserExtraTypeId(SecurityStandard.UserExtraType.siteCooporation.getId());
			partnerCriteria.setDataFetchMode(CommonStandard.DataFetchMode.full.toString());
			List<User> partnerList = partnerService.list(partnerCriteria);
			logger.debug("找到了[" + (partnerList == null ? -1 : partnerList.size()) + "个网站联运方");


			if(partnerList != null && partnerList.size() > 0){
				for(User partner : partnerList){
					if(partner.getUserConfigMap() == null){
						continue;
					}
					boolean multiDomainUsed = false;
					for(UserData userData : partner.getUserConfigMap().values()){
						logger.debug("比对数据:" + userData.getDataCode() );
						if(!userData.getDataCode().equals(CommonStandard.DataName.supplierDomain.toString())){
							continue;
						}
						String[] domains = userData.getDataValue().split(",");
						for(String domain : domains){
							String[] domainAndSystemName = domain.split(":");
							logger.debug(request.getServerName() + ":" + domainAndSystemName[0]);

							if(request.getServerName().endsWith(domainAndSystemName[0])){
								siteDomain = domainAndSystemName[0];
								logger.debug("将域名设置为合作方域名:" + siteDomain );
								multiDomainUsed = true;
								if(siteUseMultiSystemName && domainAndSystemName.length == 2){
									logger.debug("将系统名称设置为合作方域名:" + siteDomain );
									systemName = domainAndSystemName[1];
								}
								break;
							}

						}
					}
					//使用theme
					if(siteUseMultiTheme && multiDomainUsed){
						for(UserData userData : partner.getUserConfigMap().values()){
							if(userData.getDataCode().equals(DataName.supplierTheme.toString())){
								theme = userData.getDataValue();
								break;
							}
						}

					}
				}
			}
		}

		if(theme == null){
			theme = configService.getValue(CommonStandard.DataName.siteTheme.toString());
		}
		if(theme != null){
			logger.debug("设置系统主题:" + theme);
			map.put("theme", theme);
		}*/



	}

	private boolean userIpIsForbidden(HttpServletRequest request) {
		/*IpPolicyCriteria ipPolicyCriteria = new IpPolicyCriteria();

		String realIp = null;
		try{
			realIp = request.getHeader("X-Real-IP");
		}catch(Exception e){

		}
		if(StringUtils.isBlank(realIp)){
			realIp = request.getRemoteAddr();
		}
		ipPolicyCriteria.setIpAddress(realIp);
		return ipPolicyService.isForbidden(ipPolicyCriteria);*/
		return false;
	}

	//从缓存或数据库中获取前端配置
	private FrontSetting getFrontSetting(long ownerId){
		FrontSetting frontSetting = null;
		if(frontSettingCache != null && frontSettingCache.size() > 0){
			frontSetting = frontSettingCache.get(ownerId);
			if(frontSetting != null){
				logger.debug("从缓存中获取到ownerId=" + ownerId + "的前端配置信息:" + frontSetting);
				return frontSetting;
			}
		}
		frontSetting = new FrontSetting();

		String theme = configService.getValue(DataName.frontTheme.toString(), ownerId);
		frontSetting.theme = (theme == null ? Constants.DEFAULT_THEME_NAME : theme);


		String moneyName = configService.getValue(DataName.moneyName.toString(), ownerId);		
		frontSetting.moneyName = moneyName;//(moneyName == null ? MoneyType.money.getName() : moneyName);

		String coinName = configService.getValue(DataName.coinName.toString(), ownerId);
		frontSetting.coinName = coinName;//(coinName == null ? MoneyType.coin.getName() : coinName);

		String pointName = configService.getValue(DataName.pointName.toString(), ownerId);
		frontSetting.pointName = pointName;// (pointName == null ? MoneyType.point.getName() : pointName);

		String scoreName = configService.getValue(DataName.scoreName.toString(), ownerId);
		frontSetting.scoreName = scoreName;//(scoreName == null ? MoneyType.score.getName() : scoreName);

		String loginUrl = configService.getValue(DataName.frontLoginUrl.toString(), ownerId);
		frontSetting.loginUrl = (loginUrl == null ? SiteConstants.FRONT_LOGIN_URL : loginUrl);

		String systemName = configService.getValue(DataName.systemName.toString(), ownerId);
		frontSetting.systemName = systemName;

		String commonFooter = configService.getValue(DataName.commonFooter.toString(),ownerId);
		frontSetting.commonFooter = (commonFooter == null ? ("版权所有 &copy; " + year + " " + systemName) : HtmlTagTrim.trimWithBr(commonFooter));



		logger.debug("把ownerId=" + ownerId + "的前端配置信息:" + frontSetting + "放入缓存");
		synchronized(this){
			frontSettingCache.put(ownerId, frontSetting);
		}
		return frontSetting;
	}

}

class FrontSetting {

	public String theme;
	public String moneyName;
	public String coinName;
	public String pointName;
	public String scoreName;
	public String loginUrl;
	public String systemName;
	public String commonFooter;

	@Override
	public String toString() {
		return new StringBuffer().append(getClass().getName()).append('@').append(Integer.toHexString(hashCode())).append('(').append("systemName=").append(systemName).append("',").append("theme=").append("'").append(theme).append("',").append("moneyName=").append("'").append(moneyName).append("',").append("coinName=").append("'").append(coinName).append("',").append("pointName=").append(pointName).append("',").append("scroeName=").append(scoreName).append("',").append("loginUrl=").append(loginUrl).append("')").toString();
	}

}
