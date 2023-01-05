package com.maicard.boss.controller.abs;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.Operate;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.CacheValue;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.CacheService;
import com.maicard.core.service.CenterDataService;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.entity.OperateLog;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.security.entity.User;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.OperateLogService;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerService;
import com.maicard.security.service.SecurityLevelService;
import com.maicard.site.service.CaptchaService;
import com.maicard.utils.CookieUtils;
import com.maicard.utils.HttpUtils;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.RSAForJs;
import com.maicard.utils.SecurityLevelUtils;
import com.maicard.utils.StringTools;

/**
 * 管理侧用户控制器
 * @author NetSnake
 * @date 2019-08-10
 *
 */
@Controller
public class BossAbstractUserController  extends ValidateBaseController{

	@Resource
	protected CacheService cacheService;
	@Resource
	protected CaptchaService captchaService;
	@Resource
	protected CertifyService certifyService;

	@Resource
	protected CenterDataService centerDataService;

	@Resource
	protected SecurityLevelService securityLevelService;
	@Resource
	protected OperateLogService operateLogService;


	@Resource
	protected PartnerRoleRelationService partnerRoleRelationService;

	@Resource
	protected PartnerService partnerService;	

	protected final int securityLevelId =  SecurityLevelUtils.getSecurityLevel();


	protected String loginView = "biz/user/login";
	protected String secAuthloginView = "biz/user/secAuthLogin";
	protected final long rsaKeyTtl = 600;
	protected SecurityLevel securityLevel;
	protected final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);

	protected static CacheValue rsaCache = null;



	@PostConstruct
	public void init(){
		securityLevel = securityLevelService.select(securityLevelId);
		

	}


	

	public static String decodeStr(String encodeStr) {
		byte[] b = encodeStr.getBytes();
		Base64 base64 = new Base64();
		b = base64.decode(b);
		String s = new String(b);
		return s;
	}





	//用户退出登录
	@IgnorePrivilegeCheck
	@RequestMapping(value="/logout", method=RequestMethod.GET)	
	public String logout(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws IOException {
		final String logoutRedirectTo = "/";
		//SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		logger.debug("处理合作伙伴退出");
		User partner = certifyService.getLoginedUser(request, response, UserTypes.partner.id);
		if(partner == null){
			logger.debug("请求中未找到合作伙伴，无需退出.");
			response.sendRedirect(logoutRedirectTo);
		}
		//cookieService.removeCookie(request, response, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_p", siteDomainRelation.getCookieDomain());
		certifyService.logout(request, response, partner);
		request.getSession().invalidate();
		map.clear();
		response.sendRedirect(logoutRedirectTo);
		return null;
	}

	@RequestMapping(value="/login", method=RequestMethod.GET)
	@IgnorePrivilegeCheck
	public String getLogin(HttpServletRequest request,HttpServletResponse response, ModelMap map) throws Exception {

		certifyService.getRemeberMeStatus(request, response, map);

		boolean needCaptcha = needCaptchaForLogin(request);
		long ownerId = 0;
		String x509Name = null;

		writeRsa(request,map);

		try{
			ownerId = (long)map.get("ownerId");
		}catch(Exception e){
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		if(ownerId < 1){
			logger.error("系统会话中没有ownerId数据");
			return ViewNames.partnerMessageView;
		}

		if(securityLevelId >= SecurityLevel.SECURITY_LEVEL_DB3){
			try{
				X509Certificate[]   certs   =   (X509Certificate[])   request.getAttribute( "javax.servlet.request.X509Certificate");
				//logger.info("得到" + certs.length + "个证书");
				if(certs.length > 0){
					String[] dn = certs[0].getSubjectDN().getName().split(",");
					for(int i = 0; i < dn.length; i++){
						String[] tempPair = dn[i].split("=");
						if(tempPair[0].trim().equals("CN")){
							x509Name = tempPair[1].trim();
						}
					}
				}
			}catch(NullPointerException e){
				logger.warn("未能得到用户浏览器证书");
			}
			logger.debug("系统强制要求X509证书登录，得到的证书CN=" + x509Name);
			if(x509Name != null){
				map.put("x509Name", x509Name);
			} 
		}

		map.put("needCaptcha", needCaptcha);
		return loginView;
	}



	//用户登录
	@RequestMapping(value="/login", method=RequestMethod.POST)
	@IgnorePrivilegeCheck
	public String login(HttpServletRequest request, HttpServletResponse response, ModelMap map, 
			final User user) throws Exception {

		long ownerId = 0;
		String x509Name = null;
		int captchaInputErrorCount = 0;
		boolean needCaptcha = false;

		try{
			ownerId = (long)map.get("ownerId");
		}catch(Exception e){
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		if(ownerId < 1){
			logger.error("系统会话中没有ownerId数据");
			return loginView;
		}
		String decryptedPassword = decryptPassword(user.getUserPassword());
		writeRsa(request,map);
		if(decryptedPassword == null){
			logger.error("RSA解密密码失败");
			return secAuthloginView;
		}


		if(securityLevelId >= SecurityLevel.SECURITY_LEVEL_DB3){
			try{
				X509Certificate[]   certs   =   (X509Certificate[])   request.getAttribute( "javax.servlet.request.X509Certificate");
				//logger.info("得到" + certs.length + "个证书");
				if(certs.length > 0){
					String[] dn = certs[0].getSubjectDN().getName().split(",");
					for(int i = 0; i < dn.length; i++){
						String[] tempPair = dn[i].split("=");
						if(tempPair[0].trim().equals("CN")){
							x509Name = tempPair[1].trim();
						}
					}


				}

			}catch(NullPointerException e){}
			if(x509Name != null){
				user.setUsername(x509Name);
				user.setUuid(0);
			} else {
				map.put("message", new EisMessage(OpResult.failed.getId(),EisError.certifyNotFoundInReuest.id, "对不起，找不到您的证书。"));
				saveLoginLog(user.getUsername(), EisError.certifyNotFoundInReuest.id, request, ownerId);
				return loginView;
			}
		}
		needCaptcha = needCaptchaForLogin(request);


		//检查用户登录次数
		int userMayLogin = checkUserMayLogin(user.getUsername(), request);
		if(userMayLogin != 0){
			StringBuffer sb = new StringBuffer();
			sb.append("对不起，暂不允许登录");
			if(userMayLogin > 0){
				sb.append(",请等待" + userMayLogin + "秒后重试");
			}
			logger.debug("由于当前系统限制，不允许用户[" + user.getUsername() + "]登录");
			map.put("message", new EisMessage(OpResult.failed.getId(),EisError.ACCOUNT_LOCKED.id, sb.toString()));
			//FIXME 不允许登录的情况，是否也记录？
			saveLoginLog(user.getUsername(), EisError.ACCOUNT_LOCKED.id, request,ownerId);
			return loginView;
		}
		//OperateLogCriteria operateLogCriteria = new OperateLogCriteria();

		/*int showCaptchaWhenLoginFailCount = configService.getIntValue(DataName.showCaptchaWhenLoginFailCount.toString(), ownerId);

		try{
			captchaInputErrorCount = (Integer)request.getSession().getAttribute("captchaInputErrorCount");

		}catch(Exception e){}
		logger.debug("验证码输入错误次数:" + captchaInputErrorCount + ", 系统允许的不输入验证码登录次数:" + showCaptchaWhenLoginFailCount);
		if(captchaInputErrorCount >= showCaptchaWhenLoginFailCount){
			needCaptcha = true;
		}*/
		/*if(showCaptchaWhenLoginFailCount == -1){
			needCaptcha = false;			
		}*/
		boolean patchcaIsOk = false;
		if(needCaptcha){
			String userInputCaptcha = request.getParameter(DataName.captchaWord.toString());
			if(userInputCaptcha != null){
				patchcaIsOk = captchaService.verify(request, response, null, userInputCaptcha);
			}
			logger.debug("验证码校验结果:" + patchcaIsOk);	

			if(!patchcaIsOk){
				map.put("message", new EisMessage(OpResult.failed.getId(),EisError.AUTH_FAIL.id, "对不起，请输入正确的验证码。"));
				captchaInputErrorCount++;
				logger.info("验证码输入错误，增加验证码输入错误次数到Session:" + captchaInputErrorCount);
				request.getSession().setAttribute("captchaInputErrorCount", captchaInputErrorCount);
				saveLoginLog(user.getUsername(), EisError.captchaError.id, request, ownerId);
				return loginView;
			} else {

			}
		} 
		if(!needCaptcha || patchcaIsOk){
			captchaInputErrorCount = 0;
			logger.info("验证码输入正确，或不需要输入验证码，将验证码输入错误次数清零:" + captchaInputErrorCount);
			request.getSession().setAttribute("captchaInputErrorCount", captchaInputErrorCount);
		}
		user.setUserPassword(decryptedPassword);
		int result = login(user, request, response, map);
		if(result != OpResult.success.id){
			captchaInputErrorCount++;
			request.getSession().setAttribute("loginErrorCount", captchaInputErrorCount);
			map.put("message", new EisMessage(OpResult.failed.getId(),EisError.AUTH_FAIL.id, "用户名或密码错误"));
			return loginView;
		}
		certifyService.setRememberMe(request, response, map);
		EisMessage loginResult =  new EisMessage(OpResult.success.getId(),OpResult.success.id, "登录成功");

		map.put("message", loginResult);
		String returnUrl = CookieUtils.getCookie(request, Constants.COOKIE_REDIRECT_COOKIE_NAME + "_p");
		if(returnUrl == null || returnUrl.endsWith(".json")){
			returnUrl = "/";
		}
		returnUrl = java.net.URLDecoder.decode(returnUrl,"UTF-8");

		if(HttpUtils.isAjax(request) || HttpUtils.isJsonAccess(request)) {
			//不能重定向，写入
			logger.debug("登录成功，当前是ajax/json访问，返回跳转地址:" + returnUrl);
			map.clear();
			map.put("message",EisMessage.success(returnUrl));
			return loginView;
		}
		logger.debug("登录成功，将重定向到:" + returnUrl);
		response.sendRedirect(returnUrl);
		return null;
	}


	/*
	 * 已经登录的用户，在请求敏感资源的时候需要再次输入密码，进行二次验证
	 */
	@RequestMapping(value="/secAuth/login", method=RequestMethod.GET)
	@IgnorePrivilegeCheck
	public String getSecAuthLogin(HttpServletRequest request,HttpServletResponse response, ModelMap map) throws Exception {
		long ownerId = 0;
		try{
			ownerId = (long)map.get("ownerId");
		}catch(Exception e){
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		if(ownerId < 1){
			logger.error("系统会话中没有ownerId数据");
			return ViewNames.partnerMessageView;
		}

		/*User partner = certifyService.getLoginedUser(request, response, UserTypes.partner.id);
		if(partner == null){
			//无权访问
			throw new UserNotFoundInRequestException("您可能尚未登录，或会话已过期，建议您刷新页面并重新登录。");
		}*/
		writeRsa(request,map);
		map.put("strictAuthType", ServletRequestUtils.getStringParameter(request, "strictAuthType"));
		map.put("data", ServletRequestUtils.getStringParameter(request, "data"));

		String returnUrl = ServletRequestUtils.getStringParameter(request, "returnUrl");
		map.put("returnUrl", returnUrl);
		
		return secAuthloginView;
	}

	//用户提交二次验证密码进行验证
	@RequestMapping(value="/secAuth/login", method=RequestMethod.POST)
	@IgnorePrivilegeCheck
	public String secAuthlogin(HttpServletRequest request, HttpServletResponse response, ModelMap map, 
			String userPassword) throws Exception {

		long ownerId = 0;
		try{
			ownerId = (long)map.get("ownerId");
		}catch(Exception e){
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		if(ownerId < 1){
			logger.error("系统会话中没有ownerId数据");
			return secAuthloginView;
		}
		

		map.put("strictAuthType", ServletRequestUtils.getStringParameter(request, "strictAuthType"));
		map.put("data", ServletRequestUtils.getStringParameter(request, "data"));
		
		String returnUrl = ServletRequestUtils.getStringParameter(request, "returnUrl");
		map.put("returnUrl", returnUrl);

		
		User partner = certifyService.getLoginedUser(request, response, UserTypes.partner.id);
		if(partner == null){
			logger.error("在会话中找不到对应的用户");
			map.put("message", new EisMessage(OpResult.failed.getId(),EisError.userNotFoundInSession.id, "请先登录"));
			return secAuthloginView;
		}
		String decryptedPassword = decryptPassword(userPassword);
		writeRsa(request,map);
		if(decryptedPassword == null){
			logger.error("RSA解密密码失败");
			return secAuthloginView;
		}

		//int rs = certifyService.checkSecAuth(request, response, partner, decryptedPassword);
		int rs = certifyService.strictAuthorize(request, response, partner, decryptedPassword);

		if(rs != OpResult.success.id){
			map.put("message", new EisMessage(OpResult.failed.getId(),rs, "验证失败"));
			return secAuthloginView;
		}
		map.put("message", new EisMessage(OpResult.success.getId(),OpResult.success.id, "验证成功"));
		if(returnUrl == null || returnUrl.endsWith(".json")){
			returnUrl = "/";
		}
		returnUrl = java.net.URLDecoder.decode(returnUrl,"UTF-8");
		if(HttpUtils.isAjax(request) || HttpUtils.isJsonAccess(request)) {
			//不能重定向，写入
			logger.debug("登录成功，当前是ajax/json访问，返回跳转地址:" + returnUrl);
			map.clear();
			map.put("message",EisMessage.success(returnUrl));
			return loginView;
		}
		logger.debug("二次验证成功，将重定向到:" + returnUrl);
		response.sendRedirect(returnUrl);
		return null;
	}

	protected String decryptPassword(String userPassword) {
		KeyPair kp = null; 

		//如果缓存对象不为空并且没有超时，就获取缓存中的数据
		if(rsaCache != null){
			kp = (KeyPair)rsaCache.value;
		} 
		if(kp == null){
			logger.error("缓存中的数据不是RSA密钥对数据");
			return null;
		}
		RSAPrivateKey protectedKey = (RSAPrivateKey)kp.getPrivate();
		
		String decryptedPassword = null;
			decryptedPassword = RSAForJs.decrypt(userPassword, protectedKey);
		
		logger.debug("对加密密码[" + userPassword + "]解密后:" + decryptedPassword);		
		if(decryptedPassword == null){
			return null;
		}
		//前端security.js会把明文反向，所以这里也要对应的反向一下
		return new StringBuffer().append(decryptedPassword).reverse().toString();
	}



	/*
	 * 检查用户是否能登录
	 * 如果不能登录将返回一个秒数，告诉用户需要等待多久
	 * 
	 */
	protected int checkUserMayLogin(String username, HttpServletRequest request) {
		if(StringUtils.isBlank(username)){
			logger.warn("用户名为空，不允许提交登陆");
			return -1;
		}
		CriteriaMap params = CriteriaMap.create();
		params.put("userTypeId",UserTypes.partner.id);
		params.put("username",username);
		params.put("currentStatus",UserStatus.normal.id);

		List<User> partnerList = partnerService.list(params);
		if(partnerList == null || partnerList.size() < 1){
			logger.warn("找不到用户名为[" + username + "]的正常状态partner");
			return -1;
		}
		User partner = partnerList.get(0);
		params.clear();
		params.put("uuid",partner.getUuid());
		params.put("operateCode",Operate.login.name());
		params.put("objectType",ObjectType.partner.name());
		if(securityLevel == null){
			logger.warn("系统没有指定级别[" + securityLevelId + "]的安全级别，允许登录");
			return 0;
		}
		//查询指定时间内的用户登陆记录
		int queryDuration = NumericUtils.getNumeric(securityLevel.getData().get(DataName.loginRetryDurationSec.toString()));
		if(queryDuration < 1){
			logger.debug("当前安全级别[" + securityLevel + "]未定义检查多久以内的登录次数");
		} else {
			int maxCount = NumericUtils.getNumeric(securityLevel.getData().get(DataName.loginRetryCountInDuration.toString()));
			if(maxCount < 1){
				logger.debug("当前安全级别[" + securityLevel + "]未定义在" + queryDuration + "秒内登录失败次数的上限");

			} else {
				params.put("beginTime",DateUtils.addSeconds(new Date(), -queryDuration));
				//得到指定时间内该用户的所有登录操作
				List<OperateLog> loginLogList = operateLogService.list(params);
				logger.debug("自[" + sdf.format(params.get("beginTime")) + "]开始，用户[" + partner.getUuid() + "]的登录次数是:" + (loginLogList == null ? "空" : loginLogList.size()));
				//筛选出失败的登录
				int failCount = 0;
				for(OperateLog logLog : loginLogList){
					int operateResult = NumericUtils.getNumeric(logLog.getOperateResult());
					if(operateResult != OpResult.success.id){
						failCount++;
					}
				}
				if(failCount >= maxCount){
					logger.debug("用户[" + partner.getUuid() + "]自[" + StringTools.time2String(params.get("beginTime")) + "]开始登录失败次数[" + failCount + "]已超过当前安全级别[" + securityLevel + "]的限制:" + maxCount);
					return queryDuration;
				}
				logger.debug("用户[" + partner.getUuid() + "]自[" + StringTools.time2String(params.get("beginTime")) + "]开始登录失败次数[" + failCount + "]还没超过当前安全级别[" + securityLevel + "]的限制:" + maxCount);

			}
		}
		return 0;
	}

	protected int login(User user, HttpServletRequest request, HttpServletResponse response, ModelMap map) {

		String loginUserName = user.getUsername();
		logger.debug("User[" + loginUserName + "/" + user.getUserPassword() + "] try login.");
		if(user.getUsername() == null || user.getUserPassword() == null){
			logger.error("Passport username or password null.");
			map.put("json", new EisMessage(OpResult.failed.getId(),EisError.usernameOrPasswordIsNull.id, "对不起，请检查您的用户名或密码。"));
			saveLoginLog(user.getUsername(), EisError.usernameOrPasswordIsNull.id, request, user.getOwnerId());
			return EisError.usernameOrPasswordIsNull.id;		
		}
		if(user.getUsername().equals("") || user.getUserPassword().equals("")){
			logger.error("Passport username or password empty.");
			map.put("json", new EisMessage(OpResult.failed.getId(),EisError.usernameOrPasswordIsNull.id, "对不起，请检查您的用户名或密码。"));
			saveLoginLog(user.getUsername(), EisError.usernameOrPasswordIsNull.id, request, user.getOwnerId());
			return EisError.usernameOrPasswordIsNull.id;		
		}
		CriteriaMap userCriteria = CriteriaMap.create();
		userCriteria.put("username",user.getUsername());
		userCriteria.put("userPassword",user.getUserPassword());
		userCriteria.put("currentStatus",BasicStatus.normal.id);
		userCriteria.put("userTypeId",UserTypes.partner.id);
		user = certifyService.login(request, response, userCriteria);
		
		if(user == null){
			logger.info("用户登录失败");
			saveLoginLog(loginUserName, EisError.AUTH_FAIL.id, request, (long)map.get("ownerId"));
			return EisError.AUTH_FAIL.id;		
		}
		//logger.info("登录合作伙伴的配置数据有[" + (user.getUserData() == null ? 0 : user.getUserData().size()) + "]条");




		//userService.updateLoginSync(user);
		map.put("message", new EisMessage(OpResult.success.getId(),OpResult.success.id, "登录成功"));
		saveLoginLog(user.getUsername(), OpResult.success.id, request, user.getOwnerId());

		map.put("user", user);
		request.getSession().setAttribute(Constants.SESSION_USER_NAME, user);
		request.getSession().setAttribute(Constants.SESSION_TOKEN_NAME, user.getUuid());

		//将用户的密码替换为明文并发送到消息总线
		user.setUserPassword(user.getUserPassword());

		/*String bbsLoginString = loginBbs(user);			
			if(bbsLoginString != null){
				if(user.getUserConfigMap() == null){
					user.setUserConfigMap(new HashMap<String,UserConfig>());
				}
				UserConfig bbs = new UserConfig();
				bbs.setDataCode(Constants.dataNameSiteBbsLoginUrl);
				bbs.setDataValue(bbsLoginString);
				user.getUserConfigMap().put(bbs.getDataCode(), bbs);
			}*/
		return OpResult.success.id;		

	}

	/*
	 * 用户登录时是否需要验证码
	 */
	protected boolean needCaptchaForLogin(HttpServletRequest request){
		boolean needCaptcha = true;
		if(securityLevelId >= SecurityLevel.SECURITY_LEVEL_STRICT){
			logger.debug("当前系统安全级别是:" + securityLevelId + ",强制实行验证码登录");
			needCaptcha = true;
		} else {

		}
		request.getSession().setAttribute("needCaptcha", needCaptcha);

		return needCaptcha;

	}


	/*
	 * 获取RSA密钥对
	 * 并向Session中写入私钥系数和指数，供提交时解密
	 * 向前端map中写入公钥系数和指数，供前端security.js加密
	 */
	protected void writeRsa(HttpServletRequest request, ModelMap map) {
		KeyPair kp = null; 

		//如果缓存对象不为空并且没有超时，就获取缓存中的数据
		if(rsaCache != null && !rsaCache.isExpired()){
			kp = (KeyPair)rsaCache.value;
		} else {
			kp = generateAndSaveKeyPair();
		}


		if(kp == null){
			logger.error("无法生成RSA密钥对");
		} else {
			RSAPublicKey publicKey = (RSAPublicKey)kp.getPublic();
			map.put(DataName.publicKeyModulus.toString(), publicKey.getModulus().toString(16));
			map.put(DataName.publicKeyExponent.toString(), publicKey.getPublicExponent().toString(16));
			logger.debug("111向map中放入公钥系数:" + publicKey.getModulus().toString() + ",公钥指数:" + publicKey.getPublicExponent().toString());

		}		
	}

	protected KeyPair generateAndSaveKeyPair(){

		
		KeyPair kp = RSAForJs.genKeyPair();
		rsaCache = new CacheValue(null,kp, DateUtils.addSeconds(new Date(), (int)rsaKeyTtl));
		logger.debug("生成新的RSA密钥对并放入缓存，有效期:" + sdf.format(rsaCache.expireTime));
		return kp;
	}

	/*
	 * 保存登录失败和错误的日志
	 */
	protected void saveLoginLog(String username, int statusCode, HttpServletRequest request, long ownerId) {
		long uuid = 0;
		CriteriaMap userCriteria = CriteriaMap.create();
		userCriteria.put("username",username);
		List<User> partnerList = partnerService.list(userCriteria);
		if(partnerList != null && partnerList.size() > 0){
			uuid = partnerList.get(0).getUuid();
		}
		if(uuid < 1) {
			return;
		}
		try {
			//operateLogService.insert(new OperateLog(ObjectType.partner.name(),null, uuid, Operate.login.name(), String.valueOf(statusCode), null, IpUtils.getClientIp(request), configService.getServerId(), ownerId));
		} catch (Exception e) {
			e.printStackTrace();
		}			

	}



}
