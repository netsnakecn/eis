package com.maicard.boss.aspect;



import static com.maicard.site.constants.SiteConstants.DEFAULT_PAGE_SUFFIX;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.utils.*;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;

import com.alibaba.fastjson.JSON;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.boss.annotations.InternalUserOnly;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.Operate;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.security.annotation.IgnoreLoginCheck;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import com.maicard.security.annotation.RequestPrivilege;
import com.maicard.security.annotation.StrictAuthorize;
import com.maicard.security.entity.OperateLog;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.security.entity.User;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.OperateLogService;
import com.maicard.security.service.SecurityLevelService;
import com.maicard.site.service.SiteDomainRelationService;

/**
 * Partner系统切面
 * 
 * @author NetSnake
 * @date 2014-02-04
 */
@SuppressWarnings("unused")
@Aspect
public class PartnerAspect extends BaseService{

	@Resource
	private AuthorizeService authorizeService;
	@Resource
	private CertifyService certifyService;
	@Resource
	private ConfigService configService;


	@Resource
	private ApplicationContextService applicationContextService;	

	@Resource
	private AuthorizeService partnerAuthorizeService;
	@Resource
	private SecurityLevelService securityLevelService;
	@Resource
	private SiteDomainRelationService siteDomainRelationService;

	@Resource
	private OperateLogService operateLogService;

	private static Map<String, PartnerSetting> partnerSettingCache = new ConcurrentHashMap<String, PartnerSetting>();


	private final int securityLevelId =  SecurityLevelUtils.getSecurityLevel();

	boolean bossUsePageShowException = false;
	private final String loginUrl = "/user/login.shtml";

	private String aesKey = null;



	private static String systemCode;

	//二次验证登录
	private static final String secAuthloginUrl = "/user/secAuth/login.shtml";


	private static final String year = new SimpleDateFormat("yyyy").format(new Date());

	private final String partnerAspectExpress = "( execution(* com..*.boss.controller..*.*(..)) || execution(* com..*.boss.controller..*.*(..)) ) && @annotation(org.springframework.web.bind.annotation.RequestMapping)";


	private final String[] ignoreRedirectUri = new String[]{"/weixinUser/",  "/content/user/login" + DEFAULT_PAGE_SUFFIX,"/content/user/register" + DEFAULT_PAGE_SUFFIX,  "/captcha" + DEFAULT_PAGE_SUFFIX};

	private SecurityLevel securityLevel;

	/**
	 * 记录用户日志，最多存放多少请求数据
	 */
	private final int MAX_RECORD_LENGTH = 1024;
	static String GLOBAL_LOCALE = null;

	@PostConstruct
	public void init(){
		securityLevel = securityLevelService.select(securityLevelId);
		systemCode = configService.getSystemCode();
		GLOBAL_LOCALE = configService.getValue(DataName.GLOBAL_LOCALE.name(),0);

		try {
			aesKey = SecurityUtils.readAesKey();
		} catch (Exception e) {
			logger.error("无法读取系统AES密钥");
		}
	}

	@Around(partnerAspectExpress)
	public Object doAroundForPartnerSecurityCheck(ProceedingJoinPoint joinPoint) throws Throwable{

		HttpServletRequest request = null;
		HttpServletResponse response = null;
		ModelMap map = null;


		try{
			request = (HttpServletRequest)joinPoint.getArgs()[0];
			response = (HttpServletResponse)joinPoint.getArgs()[1];
			map = (ModelMap)joinPoint.getArgs()[2];
		}catch(Exception e){}
		if(request == null || response == null){
			logger.debug("方法参数不规范:" + joinPoint.getTarget().getClass().getSimpleName() + ", method:" +joinPoint.getSignature().getName() + ",参数个数:" +  joinPoint.getArgs().length);
			throw new EisException("系统request或response异常");
		}
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + request.getServerName() + "]找不到对应的站点对应数据");
			return notFoundResponse(request,response);
		}
		long ownerId = siteDomainRelation.getOwnerId();
		logger.debug("当前访问Partner系统的ownerId=" + ownerId);
		if(ownerId < 1){
			return notFoundResponse(request,response);
		}

		boolean writeOperateLog = false;
		String operateCode = request.getMethod().toUpperCase();
		if(securityLevelId >= SecurityLevel.SECURITY_LEVEL_STRICT){
			writeOperateLog = true;
		} else if(operateCode.equals("POST")){
			//对于要求不严格的，至写入POST方法的日志
			writeOperateLog = true;
		}		




		if(securityLevelId >= SecurityLevel.SECURITY_LEVEL_STRICT){
			boolean userIpIsForbidden = userIpIsForbidden(request, ownerId);
			if(logger.isDebugEnabled()){
				logger.debug("检查用户IP是否合法，检查结果:" + userIpIsForbidden);
			}
			if(userIpIsForbidden){
				if(logger.isDebugEnabled()){
					logger.debug("用户IP不合法，直接返回:" + HttpStatus.NOT_FOUND.value());
				}
				response.setStatus(HttpStatus.NOT_FOUND.value());
				joinPoint = null;
				return null;
			}
		}
		if(map != null){
			this.writePartnerSetting(map, ownerId);
		}
		String executeMethod = joinPoint.getSignature().getName();
		boolean isIgnorePrivilegeCheck = false;

		Signature  signature = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;  
		Method method = methodSignature.getMethod();  

		User partner = certifyService.getLoginedUser(request, response, UserTypes.partner.id);

		boolean jsonAccess = request.getRequestURI().toLowerCase().endsWith(".json");

		/**
		 * 是否通过注解完成了权限检查并通过
		 */
		boolean privilegeAnnotationValid = false;
		//注解方式的权限配置，如果使用了这种模式，则不检查REST模式
		RequestPrivilege requestPrivilege = AnnotationUtils.getAnnotation(method, RequestPrivilege.class);
		if(requestPrivilege != null){
			String objectTypeCode = requestPrivilege.object();
			String operateCodeTemp = requestPrivilege.operate();
			logger.info("权限注解请求操作权限{}=>{}",objectTypeCode,operateCodeTemp);
			if(StringUtils.isEmpty(objectTypeCode) || StringUtils.isEmpty(operateCodeTemp)){
				return unAuthResponse(map,request,response);

			}

			if(partner == null) {
				logger.info("当前定义了操作权限:" +objectTypeCode + "=>"+operateCodeTemp + "但没有登录");
				return unAuthResponse(map,request,response);

			}

			if(!authorizeService.havePrivilege(partner, objectTypeCode, operateCodeTemp)){
				logger.info("用户："+partner.getUuid()+"对操作"+objectTypeCode+":"+operateCodeTemp+"没有权限");
				return unAuthResponse(map,request,response);

			}
			//当定义了注解权限后就不检查REST权限
			isIgnorePrivilegeCheck = true;
			privilegeAnnotationValid = true;
		} else {
			//使用REST检查
			if(method.isAnnotationPresent(IgnorePrivilegeCheck.class) || method.isAnnotationPresent(IgnoreLoginCheck.class)){
				isIgnorePrivilegeCheck = true;
			}

		}

		
		//检查是否使用了内部用户专用注解
		InternalUserOnly internalOnly = AnnotationUtils.getAnnotation(method, InternalUserOnly.class);
		if(internalOnly == null) {
			AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), InternalUserOnly.class);
		} 
		if(internalOnly != null) {
			//确认只有内部用户能访问
			boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
			if(!isPlatformGenericPartner) {
				logger.info("用户："+partner.getUuid()+"类别是:" + partner.getUserExtraTypeId() + "，无权访问内部用户专用API:"+joinPoint.getTarget().getClass().getName() + "." + executeMethod);
				return unAuthResponse(map,request,response);

			}
			
		}
		CriteriaMap privilegeCriteria = SecurityUtils.uri2PrivilegeParams(request);


		if(isIgnorePrivilegeCheck || privilegeAnnotationValid ){
			if(privilegeAnnotationValid) {
				logger.debug("[" + joinPoint.getTarget().getClass().getName() + "." + executeMethod + "]有权限注解并通过了检查,不再进行权限检查");

			} else {
				logger.debug("[" + joinPoint.getTarget().getClass().getName() + "." + executeMethod + "]有IgnorePrivilegeCheck或IgnoreLoginCheck注解，不再进行权限检查");
			}
			//检查提交数据的合法性
			if(!inputIsLegal(request)){
				//对于严格环境，不合法输入写入日志
				if(writeOperateLog){
					writeOperateLog(request,response, privilegeCriteria.getStringValue("operateTypeCode"), privilegeCriteria.getStringValue("operateCode"), EisError.DATA_ILLEGAL.name() + "-" + EisError.DATA_ILLEGAL.id, partner);

				}
				return unAuthResponse(map,request,response);
			}
			return joinPoint.proceed();
		}

		if(partner == null){
			logger.info("未找到登录用户");
			return unAuthResponse(map,request,response);

		}
		if(partner.getUserTypeId() != UserTypes.partner.id){
			logger.error("尝试登录的用户[" + partner + "]不是partner类型");
			CookieUtils.addCookie(request, response, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_p", java.net.URLEncoder.encode(request.getRequestURI(),"UTF-8"), Constants.COOKIE_MAX_TTL,siteDomainRelation.getCookieDomain());
			return unAuthResponse(map,request,response);

		}

		if(logger.isDebugEnabled()){
			logger.debug("执行安全检查[" + request.getMethod() + "到URI:" + request.getRequestURI() + ",类:" + joinPoint.getTarget().getClass().toString() + ",方法:" + joinPoint.getSignature().getName() + "]");
		}
		if(!authorizeService.havePrivilege(request, response, UserTypes.partner.id)){
			if(writeOperateLog){
				writeOperateLog(request,response, privilegeCriteria.getStringValue("operateTypeCode"), privilegeCriteria.getStringValue("operateCode"), (OpResult.deny.name() + OpResult.deny.id),  partner);
				//无权访问，写入日志
				/*operateLogService.insert(
						new OperateLog(privilegeCriteria.getObjectTypeCode() == null ? ObjectType.unknown.name() : privilegeCriteria.getObjectTypeCode() ,
								privilegeCriteria.getObjectId(), 
								partner == null ? 0 : partner.getUuid(), 
										privilegeCriteria.getOperateCode() == null ? operateCode + " " + Operate.unknown.code :	 operateCode + " " + privilegeCriteria.getOperateCode(), 
												String.valueOf(QssError.ACCESS_DENY.id), 
												null, IpUtils.getClientIp(request), configService.getServerId(), ownerId));
				 */
			}
			return unAuthResponse(map,request,response);

		}


		//该方法是否指定了严格验证
		StrictAuthorize strictAuthorize = AnnotationUtils.getAnnotation(method, StrictAuthorize.class);
		if(strictAuthorize != null){
			String strictAuthType = strictAuthorize.authType();
			if(strictAuthType == null){
				strictAuthType = Constants.DEFAULT_STRICT_AUTH_MODE;
			}
			String returnUrl = request.getRequestURL().toString();
			if(request.getQueryString() != null){
				returnUrl += "?" + request.getQueryString();
			}
			long strictAuthTtl = strictAuthorize.ttl();
			//根据严格验证确定一个token
			String token = "strict_auth_" + strictAuthorize.token();
			String data = "strictAuthType=" + strictAuthType + "&strictAuthTtl=" + strictAuthTtl + "&strictAuthToken=" + token;
			logger.info("对方法:" + method.getName() + "的严格验证数据是:" + data);
			Crypt crypt = new Crypt();
			crypt.setAesKey(aesKey);
			String cryptData = crypt.aesEncryptHex(data);
			//cryptData = URLEncoder.encode(cryptData, Constants.DEFAULT_CHARSET);
			Object tokenData = request.getSession().getAttribute(token);

			String url = secAuthloginUrl + "?strictAuthType=" + strictAuthType + "&returnUrl=" + returnUrl + "&data=" + cryptData;
			if(tokenData == null){
				//之前没有做严格验证，需要重定向到验证地址
				logger.info("方法:" + method.getName() + "需要严格验证，但是Session中没有令牌:" + token + ",重定向到验证地址:" + url);
				if(HttpUtils.isJsonAccess(request)) {
					map.put("message", new EisMessage(OpResult.success.id,Operate.jump.id,url));
					return ViewNames.partnerMessageView;
				}
				response.sendRedirect(url);
				return null;
			} else {
				//如果已经做了严格验证，检查ttl，如果ttl已过期或者为0，则删除该token，下次访问需要再次做验证
				long ts = NumericUtils.parseLong(tokenData);
				long currentTs = new Date().getTime() / 1000;

				logger.debug("严格验证的写入ts是:" + ts + ",当前ts=" + currentTs + ",超时是:" + strictAuthTtl);

				if(strictAuthTtl == 0){
					//如果超时是0，则删除验证数据，下次将再次验证
					request.getSession().removeAttribute(token);
				} else {
					if(ts + strictAuthTtl < currentTs){
						logger.info("严格验证的写入时间是:" + ts + ",超时是:" + strictAuthTtl + ",已过期，删除Session中的令牌并重定向到验证地址:" + url);
						request.getSession().removeAttribute(token);
						if(HttpUtils.isJsonAccess(request)) {
							map.put("message", new EisMessage(OpResult.success.id,Operate.jump.id,url));
							return ViewNames.partnerMessageView;
						}
						response.sendRedirect(url);
						return null;
					}
				}
			}
		}
		//检查是否需要二次验证
		boolean needSecAuth = checkNeedSecAuth(request, response, partner);
		if(needSecAuth){
			map.put("message", new EisMessage(OpResult.failed.getId(),EisError.needSecAuth.id,"您当前的操作需要进行二次验证"));
			CookieUtils.addCookie(request, response, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_p", java.net.URLEncoder.encode(request.getRequestURI(),"UTF-8"), Constants.COOKIE_MAX_TTL,siteDomainRelation.getCookieDomain());
			response.sendRedirect(secAuthloginUrl);
			return null;
		}
		if(!inputIsLegal(request)){
			if(writeOperateLog){
				writeOperateLog(request,response, privilegeCriteria.getStringValue("operateTypeCode"), privilegeCriteria.getStringValue("operateCode"), EisError.DATA_ILLEGAL.name() + "-" + EisError.DATA_ILLEGAL.id, partner);
				/*
				operateLogService.insert(
						new OperateLog(privilegeCriteria.getObjectTypeCode() == null ? ObjectType.unknown.name() : privilegeCriteria.getObjectTypeCode() ,
								privilegeCriteria.getObjectId(), 
								partner == null ? 0 : partner.getUuid(), 
										privilegeCriteria.getOperateCode() == null ? operateCode + " " + Operate.unknown.code :	 operateCode + " " + privilegeCriteria.getOperateCode(), 
												String.valueOf(QssError.dataIllegal.id), 
												null, IpUtils.getClientIp(request), configService.getServerId(), ownerId));*/

			}
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}


		String uri = request.getRequestURI();

		//在安全级别高于最低级的时候，检查一个方法是否允许输出JSON,如果不允许，检查当前访问的后缀是否为json，如果是则返回404
		/*if(securityLevelId > SecurityLevelCriteria.SECURITY_LEVEL_DEMO){
			AllowJsonOutput allowJsonOutput = AnnotationUtils.getAnnotation(method, AllowJsonOutput.class);

			if(allowJsonOutput == null && uri.toLowerCase().endsWith("json")){
				logger.info("当前方法不允许输出JSON，但当前请求为JSON后缀");

				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
		}*/
		if(!uri.endsWith(DEFAULT_PAGE_SUFFIX) && !uri.equals("/") ){
		} else {
			boolean ignore = ServletRequestUtils.getBooleanParameter(request, "ir", false);
			if(!ignore){
				for(String ignoreData : ignoreRedirectUri){
					if(uri.contains(ignoreData)){
						logger.debug("当前请求[" + uri + "]与忽略数据[" + ignoreData + "]匹配，不写入重定向数据");
						ignore = true;
						break;
					}
				}
			} else {
				logger.debug("当前请求参数：{}中有ir=true参数，忽略重定向", request.getRequestURL().toString());
			}
			if(!ignore){
				if (request.getQueryString()!=null){
					uri=uri+"?"+request.getQueryString();
				}
				logger.debug("把当前请求[" + uri + "]写入重定向Cookie");
				CookieUtils.addCookie(request, response, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_p", java.net.URLEncoder.encode(uri,"UTF-8"), Constants.COOKIE_MAX_TTL, siteDomainRelation.getCookieDomain());
			}
		}
		String objectTypeCode = privilegeCriteria.getStringValue( "objectTypeCode");
		if(objectTypeCode != null && objectTypeCode.equalsIgnoreCase("partnerMenuRoleRelation")){
			//不写入日志，这是请求菜单项
			writeOperateLog = false;
		}


		Object result = joinPoint.proceed();
		String operateResultCode = null;
		if(map.get("message") != null){
			Object message = map.get("message");
			if(message instanceof EisMessage){
				EisMessage msg = (EisMessage)message;
				operateResultCode = msg.getMessage() + "-" + msg.getCode();
				//map.put("message",translate(msg));
			}
		}
		if(operateResultCode == null){
			operateResultCode = OpResult.accept.name() + "-" + OpResult.accept.id;
		}
		if(writeOperateLog){

			writeOperateLog(request, response, objectTypeCode, operateCode, operateResultCode, partner);
			/*String postData = null;
			if(operateCode.equals("POST")){
				StringBuffer sb = new StringBuffer();
				for(String key : request.getParameterMap().keySet()){
					if(sb.length() > MAX_RECORD_LENGTH){
						break;
					}
					sb.append(key).append(":").append(Arrays.toString(request.getParameterMap().get(key)));
				}
				if(sb.length() > 0){
					postData = sb.toString();
				}
			} else {
				postData = request.getQueryString();
			}

			operateLogService.insert(
					new OperateLog(privilegeCriteria.getObjectTypeCode() == null ? ObjectType.unknown.name() : privilegeCriteria.getObjectTypeCode() ,
							privilegeCriteria.getObjectId(), 
							partner == null ? 0 : partner.getUuid(), 
									privilegeCriteria.getOperateCode() == null ? operateCode + " " + Operate.unknown.code :	 operateCode + " " + privilegeCriteria.getOperateCode(), 
											operateResultCode, 
											postData, IpUtils.getClientIp(request), configService.getServerId(), ownerId));
			 */
		}


		return result;



	}

		private EisMessage translate(EisMessage eisMessage) {

			Locale locale = null;
			if(GLOBAL_LOCALE != null){
				locale = Locale.forLanguageTag(GLOBAL_LOCALE);
			}

			if(locale == null){
				locale = LocaleContextHolder.getLocale();
			}

			if (eisMessage == null || StringUtils.isBlank(eisMessage.message)) {
				return eisMessage;
			}
			MessageSource	msgBean = applicationContextService.getBeanGeneric(MessageSource.class);
			String transMsg = msgBean.getMessage(eisMessage.message, null, locale);
			logger.debug("当前地区是:" + locale + ",把消息:" + eisMessage.message + "翻译为:" + transMsg);
			if(transMsg != null){
				eisMessage.setMessage(transMsg);
			}
			return eisMessage;
		}


	/**
	 * 当权限检查失败后的走向
	 */
	private String notFoundResponse(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
		return null;
	}
	private String unAuthResponse(ModelMap map, HttpServletRequest request, HttpServletResponse response) {
		//为了保证跟正常逻辑中的结构一致
		map.clear();
		map.put("message",EisMessage.error(EisError.userNotFoundInSession.id,"请先登录"));
		return ViewNames.partnerMessageView;
	}
	private boolean userIpIsForbidden(HttpServletRequest request, long ownerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("rawtypes")
	private void writeOperateLog(HttpServletRequest request, HttpServletResponse response, String objectType, String operateCode, String operateResultCode, User partner) {
		/*
		 * boolean writeOperateLog = true; if(securityLevelId >=
		 * SecurityLevel.SECURITY_LEVEL_STRICT){ writeOperateLog = true; } else
		 * if(operateCode.equals("POST")){ //对于要求不严格的，至写入POST方法的日志 writeOperateLog =
		 * true; }
		 * 
		 * if(request == null){ logger.info("日志拦截请求不存在"); }
		 */
		if(partner  == null) {
			partner = certifyService.getLoginedUser(request, response, UserTypes.partner.id);
		}
		//String operateCode = operateLogAnnotation.operateCode();
		if(StringUtils.isBlank(objectType) || StringUtils.isBlank(operateCode)){
			logger.info("objectType={},operateCode={}有一个为空，不写操作日志", objectType, operateCode);
			return;
		}
		String methodType = request.getMethod().toUpperCase();
		Map paramMap = request.getParameterMap();
		String jsonString = JSON.toJSONString(paramMap);
		if(jsonString.length() > 512) {
			logger.debug("日志拦截获取参数：{} ... ...",jsonString.substring(0,512));
		} else {
			logger.debug("日志拦截获取参数：{}", jsonString);
		}
		//int dateType = operateLogAnnotation.dateType();
		//	Date deadlineTime = DateUtil.offset(DateUtil.date(),DateField.of(dateType),lengthOfTime);
		OperateLog operateLog = new OperateLog();
		operateLog.setValue(jsonString);
		operateLog.setObjectType(objectType);
		operateLog.setObjectId(null);

		operateLog.setOperateCode(operateCode);
		operateLog.setOperateResult(operateResultCode);
		operateLog.setIp(IpUtils.getClientIp(request));
		operateLog.setServerId(configService.getServerId());
		operateLog.setOwnerId(partner.getOwnerId());
		operateLog.setRequestMethod(methodType);
		operateLog.setOperateTime(new Date());
		//operateLog.setDeadlineTime(deadlineTime);
		if(partner == null) {
			logger.warn("尝试写入操作日志{}但用户为空", JSON.toJSONString(operateLog));
		} else {
			operateLog.setUuid(partner.getUuid());
		}
		operateLogService.insert(operateLog);

	}

	/*
	 * 检查该对象的访问是否需要二次验证
	 */
	private boolean checkNeedSecAuth(HttpServletRequest request, HttpServletResponse response, User partner) {
		CriteriaMap privilegeCriteria = SecurityUtils.uri2PrivilegeParams(request);
		if(privilegeCriteria == null){
			logger.error("无法确定当前请求URI对应的对象和操作:" + request.getRequestURI());
			throw new EisException("访问路径异常");
		}
		String objectType = privilegeCriteria.getStringValue("objectTypeCode");
		String operate = privilegeCriteria.getStringValue("operateCode");
		if(objectType == null){
			logger.debug("当前URI请求的对象是空，不进行二次验证检查");
			return false;
		}
		if(operate == null){
			logger.debug("当前URI请求的操作是空，不进行二次验证检查");
			return false;
		}
		//读取安全级别中的针对二次认证对象及其操作的配置，以URL字符串形式保存
		String secAuthConfig = SecurityLevelUtils.getConfig(securityLevel, DataName.secAuthObjectAndOperate.toString());
		if(StringUtils.isBlank(secAuthConfig)){
			logger.debug("当前安全级别[" + securityLevel + "]未配置二次验证数据，不进行二次验证检查");
			return false;
		}
		Map<String,String> secAuthConfigMap = HttpUtils.getRequestDataMap(secAuthConfig);
		if(secAuthConfigMap == null || secAuthConfigMap.size() < 1){
			logger.debug("当前安全级别[" + securityLevel + "]配置的二次验证数据无法正常解析:" + secAuthConfig + "，不进行二次验证检查");
			return false;
		}
		if(!secAuthConfigMap.containsKey(objectType)){
			logger.debug("当前安全级别[" + securityLevel + "]不需要对[" + objectType + "]进行二次验证");
			return false;
		}
		String needOperate = secAuthConfigMap.get(objectType);
		if(needOperate == null){
			logger.warn("当前安全级别[" + securityLevel + "]要求对[" + objectType + "]进行二次验证，但没有配置哪些操作需要二次验证");
			return false;
		}
		boolean needSecAuth = false;
		if(needOperate.equals('*')){
			logger.debug("当前安全级别[" + securityLevel + "]要求对[" + objectType + "]进行二次验证，并且其操作码是*，即所有操作都要进行二次验证");
			needSecAuth = true;
		} else {

			String[] needOperateList = needOperate.split(",");
			for(String needOp : needOperateList){
				if(needOp.equalsIgnoreCase(operate)){
					logger.debug("当前安全级别[" + securityLevel + "]要求对[" + objectType + "]进行二次验证，其操作码[" + needOperate + "]也包含了当前操作:" + operate + "，需要进行二次验证");
					needSecAuth = true;
					break;
				}
			}
		}
		if(needSecAuth){
			boolean alreadySecAuthed = certifyService.isSecAuthed(request, response, partner);
			return !alreadySecAuthed;
		}
		logger.debug("当前安全级别[" + securityLevel + "]要求对[" + objectType + "]进行二次验证，其操作码[" + needOperate + "]不包含当前操作:" + operate + "，不进行二次验证");		
		return false;
	}



	/*
	 * 提交的所有数据是否合法
	 */
	private boolean inputIsLegal(HttpServletRequest request) {
		/*for(String key :request.getParameterMap().keySet()){
			if(logger.isDebugEnabled()){
				logger.debug("检查输入参数[" + key + "=>" + request.getParameter(key) + "]");
			}
			if(key.equals("title")){
				logger.info("把title参数篡改为test_filter");
				request.getParameterMap().put("title", new String[]{"test_filter"});
			}
			if(!InputFilter.isLegal(key)){
				logger.debug("输入参数[" + key + "]不合法");
				return false;
			}
			if(!InputFilter.isLegal(request.getParameter(key))){
				logger.debug("输入参数[" + key + "]的数据[" + request.getParameter(key) + "]不合法");
				return false;
			}

		}
		 */
		return true;
	}

	/*	String moneyName = configService.getValue(DataName.moneyName.toString(),ownerId);
	String coinName = configService.getValue(DataName.coinName.toString(),ownerId);
	String pointName = configService.getValue(DataName.pointName.toString(),ownerId);
	String scoreName = configService.getValue(DataName.scoreName.toString(),ownerId);
	String commonFooter = configService.getValue(DataName.commonFooter.toString(),ownerId);
	String systemName = configService.getValue(DataName.systemName.toString(), ownerId);
	String theme = configService.getValue(DataName.partnerTheme.toString(), ownerId);*/

	//从缓存或数据库中获取配置
	private void writePartnerSetting(ModelMap map, long ownerId){
		PartnerSetting partnerSetting = null;
		if(partnerSettingCache != null && partnerSettingCache.size() > 0){
			partnerSetting = partnerSettingCache.get(String.valueOf(ownerId));
			if(partnerSetting != null){
				logger.debug("从缓存中获取到ownerId=" + ownerId + "的前端配置信息:" + partnerSetting);
			}
		}
		if(partnerSetting == null) {
			partnerSetting = new PartnerSetting();
			String theme = configService.getValue(DataName.partnerTheme.toString(), ownerId);
			partnerSetting.theme = (theme == null ? Constants.DEFAULT_THEME_NAME : theme);

			String systemName = configService.getValue(DataName.systemName.toString(), ownerId);
			partnerSetting.systemName = systemName;

			String commonFooter = configService.getValue(DataName.commonFooter.toString(),ownerId);
			partnerSetting.commonFooter = (commonFooter == null ? ("版权所有 &copy; " + year + " " + systemName) : HtmlUtils.trimWithBr(commonFooter));

			
			partnerSetting.searchStyle = configService.getIntValue("PARTNER_SERACH_STYLE", ownerId);
			logger.debug("把ownerId=" + ownerId + "的商户配置信息:" + partnerSetting + "放入缓存");
			synchronized(this){
				partnerSettingCache.put(String.valueOf(ownerId), partnerSetting);
			}
		}

		map.put("ownerId", ownerId);
		map.put("systemCode", systemCode);

		map.put("theme", partnerSetting.theme);
		map.put("commonFooter", partnerSetting.commonFooter);
		map.put("systemName", partnerSetting.systemName);

		map.put("moneyName", partnerSetting.moneyName);
		map.put("coinName", partnerSetting.coinName);
		map.put("pointName", partnerSetting.pointName);
		map.put("scoreName", partnerSetting.scoreName);
		if(securityLevel != null){
			map.put("securityLevelDesc", new StringBuffer().append("当前安全级别:").append(securityLevel.getName()).append(" ").append(securityLevelId).append("级").toString() );
		}
		
		map.put("searchStyle", partnerSetting.searchStyle);


	}

}


class PartnerSetting {

	/**
	 * 管理后台搜索框的类型
	 * 0 不遮掩
	 * 1 增加一个更多搜索条件的限制
	 */
	public int searchStyle;
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

