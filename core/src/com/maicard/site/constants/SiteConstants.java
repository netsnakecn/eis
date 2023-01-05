package com.maicard.site.constants;

public class SiteConstants {
	public static final String contentPrefix = "content/";
	public static final String documentSplitTag = "<hr>";
	

	//默认文件名后缀
	public static final String DEFAULT_PAGE_SUFFIX = ".shtml";

	public static final String PARTNER_LOGIN_URL = "/user/login" + DEFAULT_PAGE_SUFFIX;
	public static final String FRONT_LOGIN_URL = "/content/user/login" + DEFAULT_PAGE_SUFFIX;
	
	public static final String EXTRA_DATA_OPEN_PATH = "open";			//文档上传的附件保存目录
	public static final String EXTRA_DATA_LOGIN_PATH = "login";	//登录用户专用附件保存目录
	public static final String EXTRA_DATA_SUBSCRIBE_PATH = "subscribe";		//订阅用户专用附件保存目录
	public static final String EXTRA_DATA_TEMP = "temp";	//临时上传文件目录
	public static final String CN_TEXT_ANALIZER = "ik_max_word";


    public static final String DEFAULT_DOCUMENT_TYPE = "NORMAL";
}
