package com.maicard.core.constants;

/**
 * 应用程序的环境
 *
 *
 * @author NetSnake
 * @date 2016年6月28日
 *
 */
public enum ContextType {
	/**
	 * 微信应用环境，对支付环节来说，微信支付应直接使用NATIVE模式，支付宝使用扫码模式
	 */
	WEIXIN,
	/**
	 * 应用程序环境，微信使用NATIVE模式，支付宝使用WEB跳转模式或SDK模式
	 */
	APP,
	/**
	 * PC环境，微信和支付宝都使用扫码模式
	 */
	 PC,
	 /**
	  * WAP即H5模式，微信使用扫码模式，支付宝使用WebView直接唤起支付宝客户端模式
	  */
	 WAP;

}
