package com.maicard.views;


public class JsonFilterView {
	public static class Front {};	//前端级别
	public static class Partner  extends Front {};	//合作伙伴级别
	public static class Full extends Partner{};			//全部，标记为该级别才输出的属性，几乎不会被输出

}
