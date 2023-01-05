package com.maicard.core.constants;

import java.math.BigDecimal;

import com.maicard.site.constants.DocumentStatus;

public class Constants {
	

	//原生field
	public static final String COLUMN_TYPE_NATIVE = "native";
	//扩展数据
	public static final String COLUMN_TYPE_EXTRA = "extra";


	/**
	 * 日志中写入消息时的最大长度，防止一个日志过长
	 */
	public static final int MESSAGE_BRIEF_LENGTH = 200;
	/**
	 * 默认四舍五入
	 */
	public static final int MONEY_ROUND_TYPE = BigDecimal.ROUND_HALF_UP;
	
	/**
	 * 默认小数点位数
	 */
	public static final int MONEY_ROUND_LENGTH = 2;

	public static final int DEFAULT_PARTNER_ROWS_PER_PAGE = 20;
	
	/**
	 * 默认时间格式
	 */
	public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_STRICT_AUTH_MODE = "payPassword";

	/**
	 * 默认数值显示格式,二位小数
	 */
	public static final String DEFAULT_DECIMAL_FORMAT = "#.##";

	public static final int SESSION_DEFAULT_TTL = 36000;

	public static final String DEFAULT_CHARSET = "UTF-8";

	public static final String STAT_HOUR_FORMAT = "yyyyMMddHH";

	public static final String EIS_SECURITY_ENV_NAME = "eis.security_level";
	
	//重定向的COOKIE名称
	public static final String COOKIE_REDIRECT_COOKIE_NAME = "eis_redirect_url";
	//二次认证的Cookie名
	public static final String COOKIE_SEC_AUTH_COOKIE_NAME = "eis_sec_auth_data";
	//二次认证的默认有效期
	public static final int COOKIE_SEC_AUTH_COOKIE_DEFAULT_TTL = 1800;

	//Cookie最长有效期1年
	public static final int COOKIE_MAX_TTL = 3600 * 24 * 365;
	
	public static final String SESSION_TOKEN_NAME = "eis_passport";

	public static final String SESSION_USER_NAME = "eis_user_name";

	public static final int TOKEN_RENEW_INTERVAL = 60 * 5; //	用户令牌的最短刷新

	public static final long SSO_TIMESTAMP_DEFAULT_TTL = 600;

	public static final String ORDER_ID_TIME_FORMAT = "yyMMdd";

	public static final String implBeanNameSuffix = "Impl";
	
	
	public static final String sessionCaptchaName = "eis_captcha";
	public static final int DISTRIBUTED_LOCK_RETRY_TIME = 30;			//分布式锁的重试次数
	public static final long DISTRIBUTED_LOCK_WAIT_MS = 500;		//分布式加锁等待时间,毫秒
	public static final long DISTRIBUTED_DEFAULT_LOCK_SEC = 3;		//分布式锁的默认存活秒数
	/**
	 * 缓存的最长有效期，1年
	 */
	public static final long CACHE_MAX_TTL =  31536000;

	public static final String DEFAULT_THEME_NAME = "basic";

	public static final String CONTENT_PREFIX = "content/";
    public static final String DB_MONEY = "moneyDataSource" ;
	public static final String CACHE_NAME = "EIS_CACHE";
    public static  final String DEFAULT_SITE_CODE = "default.com";
	;

    public static String tagSplit = ",";

	public static String previewKey = "trojrytPontrojrytPontrojrytPon12";

	public static int documentDefaultStatus = DocumentStatus.drift.id;
	public static String mailBindKey = "AfephtOvho";
	public static String mailFindPasswordKey = "kigNihem";
	
	//允许一次性下载的最多数据
	public static final int MAX_ROW_LIMIT = 5000;
	
	public static final String orderIdDateFormat = "yyMMdd";
	/**
	 * 自动产生文件时，文件名中的日期部分格式
	 */
	public static final String FILE_NAME_DATE_FORMAT = "yyyyMMdd";

	
	public static final String emailPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";

	

	public static final String DEFAULT_LANGUAGE = "zh_CN";

	public static final String KEY_FILE_SUFFIX = ".qkd";

	/**
	 * 扩展数据在请求中的前缀
	 */
	public static final String DATA_KEY_PREFIX = "data-";
	
	public static final String NATIVE_KEY_PREFIX = "native-";
	public static final int DEFAULT_QUERY_DAY = 7;
	public static final long SESSION_SAVE_INTERVAL = 3600 * 24;
	
	
;


}
