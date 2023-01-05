package com.maicard.security.constants;

public class UserRelationConst {
	public static final String RELATION_LIMIT_BUILTIN = "buildin";
	public static final String RELATION_LIMIT_CUSTOM = "custom";
	public static final String RELATION_LIMIT_UNIQUE = "unique";
	public static final String RELATION_LIMIT_MULTI = "multi";
	public static final String RELATION_LIMIT_GLOBAL_UNIQUE = "globalUnique";
	
	public static final String RELATION_TYPE_READ = "read";			//阅读了该文章
	public static final String RELATION_TYPE_FAVORITE = "favorite";	//收藏了该文章
	public static final String RELATION_TYPE_PRAISE = "praise";			//点赞了该对象
	public static final String RELATION_TYPE_SUBSCRIBE = "subscribe";			//订阅了该对象

	/**
	 * 赞助了该对象
	 */
	public static final String RELATION_TYPE_DONATE = "donate";
	public static final String USER_RELATION_COUNT_KEY_PREFIX = "USER_RELATION_COUNT";
	public static final int USER_RELATION_ID_PREFIX = 92995992;

}
