package com.maicard.security.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.User;

public interface AuthorizeService {

	
	
	/**
	 * 根据request请求URI来判断用户要访问的对象和操作<br/>
	 * 与参数指定的对象类型以及对象<br/>
	 * 然后与用户拥有的Privilege进行比较<br/>
	 * 判断用户是否具有权限条件中指定的权限 
	 */
//	boolean havePrivilege(HttpServletRequest request, String objectType, Object targetObject);

	/**
	 * 根据request请求URI来判断用户要访问的对象和操作<br/>
	 * 然后与用户拥有的Privilege进行比较<br/>
	 * 判断用户是否具有权限条件中指定的权限 
	 */	
	boolean havePrivilege(HttpServletRequest request,	 HttpServletResponse response, int userTypeId);
	
	/**
	 * 判断用户是否拥有工作步骤中要操作的对象的权限
	 */
	//boolean havePrivilege(User user, long objectId, Route route);


	boolean isSuperPartner(User partner);


	String listValidObjectId(User user, String objectTypeCode,	int objectTypeId, String operateCode);

	
	/**
	 * 该用户是否一个内部管理用户<br>
	 * 是则返回true，可以不受子账户约束查看各级数据
	 * @param partner
	 * @return
	 */
	boolean isPlatformGenericPartner(User partner);


	/**
	 * 判断一个用户是否对某个对象具备指定的操作权限
	 */
	boolean havePrivilege(User user, String targetObject, String code);

	/**
	 * 返回一个以,分割的权限列表
	 * 
	 * 
	 * @author GHOST
	 * @date 2019-01-18
	 */
	String listOperateCode(User user, String objectTypeCode);

	User getUserByName(String username, int userType);

	boolean havePrivilege(User partner, CriteriaMap privilegeCriteria);



	




	
	



}
