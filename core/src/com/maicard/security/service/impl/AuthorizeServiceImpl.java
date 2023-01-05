package com.maicard.security.service.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Operate;
import com.maicard.core.constants.UserTypes;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRoleRelation;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerPrivilegeService;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerService;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.SecurityUtils;

@Service
public class AuthorizeServiceImpl extends BaseService implements AuthorizeService{
	@Resource 
	CertifyService certifyService;
	@Resource
	private PartnerService partnerService;

	@Resource
	private PartnerRoleRelationService partnerRoleRelationService;

	@Resource
	private PartnerPrivilegeService partnerPrivilegeService;

	@Override
	public String listOperateCode(User user, String objectTypeCode) {
		CriteriaMap params = CriteriaMap.create();
		params.put("uuid",user.getUuid());
		params.put("ownerId",user.getOwnerId());
		List<UserRoleRelation> userList = partnerRoleRelationService.list(params);
		if(CollectionUtils.isEmpty(userList)){
			return "";
		}
		Set<Integer> roleIds = new HashSet<Integer>();
		for(UserRoleRelation urr : userList) {
			roleIds.add(urr.getRoleId());
		}
		int[] roleIdArray= new int[roleIds.size()];
		int i = 0;
		for(int rid : roleIds) {
			roleIdArray[i] = rid;
			i++;
			
		}
		//获取权限列表
		params.clear();
		params.put("ownerId",user.getOwnerId());
		params.put("objectTypeCode",objectTypeCode);
		params.put("roleIds",roleIdArray);
		List<Privilege> list = partnerPrivilegeService.listByRole(params);
		if(CollectionUtils.isEmpty(list)){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for(Privilege privilege : list){
			if(privilege.getOperateCode().equals("*")) {
				//权限为*，直接返回所有权限
				return "*";
			}
			sb.append(privilege.getOperateCode()+",");
		}
		sb = sb.delete(sb.length()-1,sb.length());
		logger.debug("用户:{}有角色:{},对对象:{}权限列表:{}", user.getUuid(), roleIdArray, objectTypeCode, sb.toString());
		return sb.toString();
	}


	//检查指定HTTP请求的合法性
	@Override
	public boolean havePrivilege(HttpServletRequest request, HttpServletResponse response, int userTypeId) {
		User user = certifyService.getLoginedUser(request, response, userTypeId);
		logger.debug("检查用户[" + JsonUtils.toStringFull(user) + "]是否对Url有访问权限:" +  request.getRequestURI());
		if(user == null){
			return false;
		}
		//分析URL，以确定访问权限

		CriteriaMap privilegeCriteria = SecurityUtils.request2Criteria(request);
		if(privilegeCriteria == null){
			logger.error("无法解析请求URI:" + request.getRequestURI());
			return false;
		}
		privilegeCriteria.put("ownerId",user.getOwnerId());
		privilegeCriteria.put("uuid",user.getUuid());
		privilegeCriteria.put("userTypeId",user.getUserTypeId());

		return havePrivilege(user, privilegeCriteria);
	}


	@Override
	public boolean havePrivilege(User user, CriteriaMap privilegeCriteria) {
		if(user == null){
			user = initCheck(privilegeCriteria);
		}
		//	logger.info("user"+user.getUsername()+"进入啦！！！！！");
		if(user == null){
			logger.info("user对象为空！！！！！");
			return false;
		}

		long ownerId = privilegeCriteria.getLongValue("ownerId");
		String objectTypeCode = privilegeCriteria.getStringValue("objectTypeCode");
		String operateCode = privilegeCriteria.getStringValue("operateCode");
		String objectAttribute = privilegeCriteria.getStringValue("objectAttribute");
		long oid = privilegeCriteria.getLongValue("objectId");
		String objectAttributeValue = privilegeCriteria.getStringValue("objectAttributeValue");
		for(Privilege privilege : user.getRelatedPrivilegeList()){
			logger.debug("比对权限[" + privilege + "]");
			if(privilege == null){
				continue;
			}
			if(privilege.getOwnerId() != ownerId){
				logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的平台ID与条件中的不一致，跳过...");
			}
			if(StringUtils.isBlank(privilege.getObjectTypeCode())){
				logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作对象代码为空，跳过...");
				continue;
			}
			//权限与权限条件的对象类型代码一致
			if(privilege.getObjectTypeCode().equals(objectTypeCode)){
				logger.debug("比对相同的操作对象:" + objectTypeCode + "的权限[" + privilege.getId() + "/" + privilege.getOperateCode() + "]");
				/*
				 * 权限的操作代码是*
				 * 或者
				 * 权限的操作代码是r，而且权限条件的操作代码是list或get
				 * 或者
				 * 权限的操作代码是w，而且权限条件的操作代码是delete、update或create
				 * 或者
				 * 权限与权限条件的操作代码完全一致
				 */
				//logger.info("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]...");
				if(privilege.getOperateCode() == null){
					logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作码为空");
					continue;
				}					
				if(privilege.getOperateCode().equals("*")
						||(privilege.getOperateCode().equals("r") && (operateCode.equals("list") || operateCode.equals("get")))
						||(privilege.getOperateCode().equals("w") && (operateCode.equals("create") || operateCode.equals("delete") || operateCode.equals("update")))
						||privilege.getOperateCode().equals(operateCode)
						){

					logger.debug("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]与权限条件[" + operateCode + "]...");
					//如果权限objectList为空，跳过这次比对
					if(privilege.getObjectList() == null || privilege.getObjectList().equals("")){
						logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象列表为空...");
						continue;
					}
					if(privilege.getOperateCode().equals("*") && operateCode.equals(Operate.sensitive.name())) {
						//senstive权限不匹配*
						logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]是*但操作符是sensitive，不匹配");
						continue;
					}
					//如果要权限条件要比对的对象ID为空，跳过这次比对
					/*if(oid == 0){
							logger.info("权限[" + privilege.getOperateCode() + "]的对象ID为0...");
							continue;
						}*/
					String objects[] = privilege.getObjectList().split(",");
					//如果无法获取或解析匹配对象列表，跳过这次比对
					if(objects == null || objects.length == 0){
						logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode()  + "]的对象列表无法解析:" + privilege.getObjectList());
						continue;
					}
					//如果匹配对象的第一个是*，表示匹配所有
					if(objects[0].equals("*")){
						//logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象列表第一个为*，查看是否需要进行属性权限匹配...");
						//判断属性权限，如果权限的属性模式为空，那么直接返回成功
						if(privilege.getObjectAttributePattern() == null || privilege.getObjectAttributePattern().equals("") || privilege.getObjectAttributePattern().equals("*")){
							logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的属性匹配规则*，直接为真");
							return true;
						}
						//如果权限条件中未指定属性和值，则说明不需要进行判断
						if(objectAttribute == null || objectAttributeValue == null){
							logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的属性匹配内容为空，直接返回true...");
							return true;
						}
						try{
							String[] attributes = privilege.getObjectAttributePattern().split(",");
							if(attributes == null || attributes.length < 1){
								return false;
							}
							for(String attribute : attributes){
								String[] datas = attribute.split("=");
								if(datas == null || datas.length < 1){
									return false;
								}
								if(datas[0].equals(objectAttribute) && datas[1].equals(objectAttributeValue)){
									return true;
								}
							}

						}catch(Exception e){
							logger.error("在解析权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的属性匹配时出错,返回false:" + e.getMessage());
							return false;
						}
						logger.debug("当前操作与权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]操作对象匹配，但是操作码不匹配，返回false.");
						return false;
					}

					for(String objectId : objects){
						try{
							logger.debug("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象列表ID[" + objectId + "]与当前条件的对象列表:" + oid);
							if(objectId.equals(oid)){
								//判断属性权限，如果权限的属性模式为空，那么直接返回成功
								if(privilege.getObjectAttributePattern() == null || privilege.getObjectAttributePattern().equals("") || privilege.getObjectAttributePattern().equals("*")){
									return true;
								}
								//如果权限条件中未指定属性和值，则说明不需要进行判断
								if(objectAttribute == null && objectAttributeValue == null){
									logger.debug("在比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]时，权限条件中的属性和值都为空，直接返回true...");
									return true;
								}
								try{
									String[] attributes = privilege.getObjectAttributePattern().split(",");
									if(attributes == null || attributes.length < 1){
										logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象属性数据异常:" + privilege.getObjectAttributePattern());
										return false;
									}
									for(String attribute : attributes){
										logger.debug("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象属性[" + attribute + "]与当前条件的对象属性:" + objectAttribute + "=" + objectAttributeValue);
										String[] datas = attribute.split("=");
										if(datas == null || datas.length < 1){
											return false;
										}
										if(datas[0].equals(objectAttribute) && (datas[1].equals("*") || datas[1].equals(objectAttributeValue))){
											logger.debug("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的匹配条件/值与权限条件中的条件/值完全匹配，返回true...");
											return true;
										}
									}

								}catch(Exception e){
									logger.error("在解析权限的属性匹配时出错:" + e.getMessage());
									return false;
								}
							}
						}catch(Exception e){}
					}
				}
			}
		}
		logger.warn("未能匹配到与当前操作:" + privilegeCriteria + "一致的任何权限,返回false");
		return false;
	}


	/**
	 * 根据用户名或昵称得到用户UUID
	 * 如果该用户UUID是一个数字，则判断为输入的就是UUID
	 * @param username
	 * @return
	 */
	@Override
	public User getUserByName(String username, int userType) {

		
		return null;


	}

	//检查指定用户是否具备操作指定工作步骤的权限
	/*@Override
	public boolean havePrivilege(User user, long objectId, Route route){
		if(user == null){
			return false;
		}
		if(user.getUuid() < 1){
			return false;
		}
		if(route == null){
			return false;
		}

		if(user.getRelatedPrivilegeList() == null || user.getRelatedPrivilegeList().size() < 1){
			return false;
		}
		logger.info("用户[" + user.getUuid() + "]关联权限有" + user.getRelatedPrivilegeList().size() + "条.");
		for(Privilege privilege : user.getRelatedPrivilegeList()){
			if(privilege == null){
				continue;
			}
			logger.info("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]");
			if(privilege.getObjectTypeCode() == null || privilege.getObjectTypeCode().equals("")){
				logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作对象代码为空，跳过...");
				continue;
			}
			//权限与权限条件的对象类型代码一致
			if(privilege.getObjectTypeCode().equals(route.getTargetObjectType())){

	 * 权限的操作代码是*
	 * 或者
	 * 权限的操作代码是r，而且权限条件的操作代码是list或get
	 * 或者
	 * 权限的操作代码是w，而且权限条件的操作代码是delete、update或create
	 * 或者
	 * 权限与权限条件的操作代码完全一致

				//logger.info("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]...");
				if(privilege.getOperateCode() == null){
					//logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作码为空");
					continue;
				}					
				if(privilege.getOperateCode().equals("*")
						||(privilege.getOperateCode().equals("r") && (route.getTargetObjectOperateCode().equals("list") || route.getTargetObjectOperateCode().equals("get")))
						||(privilege.getOperateCode().equals("w") && (route.getTargetObjectOperateCode().equals("create") || route.getTargetObjectOperateCode().equals("delete") || route.getTargetObjectOperateCode().equals("update")))
						||privilege.getOperateCode().equals(route.getTargetObjectOperateCode())
						){
					//logger.info("比对权限[" + privilege.getOperateCode() + "]与权限条件[" + route.getTargetObjectOperateCode() + "]...");
					//如果权限objectList为空，跳过这次比对
					if(privilege.getObjectList() == null || privilege.getObjectList().equals("")){
						//	logger.info("权限[" + privilege.getOperateCode() + "]的对象列表为空...");
						continue;
					}
					//如果要权限条件要比对的对象ID为空，跳过这次比对
					if(oid == 0){
						logger.info("权限[" + privilege.getOperateCode() + "]的对象ID为0...");
						continue;
					}
					String objects[] = privilege.getObjectList().split(",");
					//如果无法获取或解析匹配对象列表，跳过这次比对
					if(objects == null || objects.length == 0){
						//	logger.info("权限[" + privilege.getOperateCode() + "]的对象列表无法解析:" + privilege.getObjectList());
						continue;
					}
					//如果匹配对象的第一个是*，表示匹配所有
					if(objects[0].equals("*")){
						//	logger.info("权限[" + privilege.getOperateCode() + "]的对象列表第一个为*，查看是否需要进行属性权限匹配...");
						//判断属性权限，如果权限的属性模式为空，那么直接返回成功
						if(privilege.getObjectAttributePattern() == null || privilege.getObjectAttributePattern().equals("") || privilege.getObjectAttributePattern().equals("*")){
							logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的对象为*，且无属性匹配，直接返回true");
							return true;
						}
						try{
							String[] attributes = privilege.getObjectAttributePattern().split(",");
							if(attributes == null || attributes.length < 1){
								logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的对象为*，但无法解析属性匹配，返回false");
								return false;
							}
							for(String attribute : attributes){
								String[] datas = attribute.split("=");
								if(datas == null || datas.length < 1){
									logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的对象为*，但属性匹配无法用=分割，返回false");
									return false;
								}
								for(String key : route.getTargetObjectAttributeMap().keySet()){
									if(datas[0].equals(key) && datas[1].equals(route.getTargetObjectAttributeMap().get(key))){
										logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的对象为*，属性[" + key + "=" + datas[1] + "]匹配一致，返回true");
										return true;
									}
								}
							}

						}catch(Exception e){
							logger.error("在解析权限的属性匹配时出错:" + e.getMessage());
							return false;
						}
						return false;
					}
					logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的对象为列表，逐个检查");
					for(String oid : objects){
						try{
							//logger.info("比对权限[" + privilege.getOperateCode() + "]的对象列表ID[" + oid + "]");
							if(Integer.parseInt(oid) == objectId){
								//判断属性权限，如果权限的属性模式为空，那么直接返回成功
								if(privilege.getObjectAttributePattern() == null || privilege.getObjectAttributePattern().equals("") || privilege.getObjectAttributePattern().equals("*")){
									return true;
								}
								try{
									String[] attributes = privilege.getObjectAttributePattern().split(",");
									if(attributes == null || attributes.length < 1){
										return false;
									}
									for(String attribute : attributes){
										String[] datas = attribute.split("=");
										if(datas == null || datas.length < 1){
											return false;
										}
										for(String key : route.getTargetObjectAttributeMap().keySet()){
											if(datas[0].equals(key) && datas[1].equals(route.getTargetObjectAttributeMap().get(key))){
												return true;
											}
										}
									}

								}catch(Exception e){
									logger.error("在解析权限的属性匹配时出错:" + e.getMessage());
									return false;
								}
							}
						}catch(Exception e){}
					}
				}
			}			
		}
		return false;

	}*/

	/**
	 * @param user 指定的操作用户
	 * @param objectTypeCode 指定的对象类型代码，在ObjectType中定义
	 * @param objectTypeId 指定的对象类型ID
	 * @param operateCode 针对该对象的操作代码
	 * 
	 * @return 返回该用户能合法操作的类的对象ID列表，以英文逗号分割的整数ID列表，*表示所有
	 */
	@Override
	public String listValidObjectId(User user, String objectTypeCode, int objectTypeId, String operateCode){
		if(user == null){			
			return null;
		}
		if(user.getRelatedPrivilegeList() == null || user.getRelatedPrivilegeList().size() < 1){
			return null;
		}
		StringBuffer validObjectIdList = new StringBuffer();
		for(Privilege privilege : user.getRelatedPrivilegeList()){
			logger.debug("比对权限[" + privilege + "]");
			if(privilege == null){
				continue;
			}
			if(StringUtils.isBlank(privilege.getObjectTypeCode())){
				logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作对象代码为空，跳过...");
				continue;
			}
			if(!objectTypeCode.equals(privilege.getObjectTypeCode())){
				logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作对象代码是:" + privilege.getObjectTypeCode() + ",与被比较对象:" + objectTypeCode + "不一致，跳过...");
				continue;
			}
			//权限与权限条件的对象类型代码一致
			if(privilege.getObjectTypeCode().equals(objectTypeCode)){
				/*
				 * 权限的操作代码是*
				 * 或者
				 * 权限的操作代码是r，而且权限条件的操作代码是list或get
				 * 或者
				 * 权限的操作代码是w，而且权限条件的操作代码是delete、update或create
				 * 或者
				 * 权限与权限条件的操作代码完全一致
				 */
				logger.info("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]...");
				if(privilege.getOperateCode() == null){
					logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "]的操作码为空");
					continue;
				}					
				if(privilege.getOperateCode().equals("*")
						||(privilege.getOperateCode().equals("r") && (operateCode.equals(Operate.list.name()) || operateCode.equals(Operate.get.name())|| operateCode.equals(Operate.relate.name()) ))
						||(privilege.getOperateCode().equals("w") && (operateCode.equals(Operate.create.name()) || operateCode.equals(Operate.delete.name()) || operateCode.equals(Operate.update.name())))
						||privilege.getOperateCode().equals(operateCode)
						){
					logger.info("比对权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]与操作码[" + operateCode + "]...");
					//如果权限objectList为空，跳过这次比对
					if(privilege.getObjectList() == null || privilege.getObjectList().equals("")){
						logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象列表为空...");
						continue;
					}

					String objects[] = privilege.getObjectList().split(",");
					//如果无法获取或解析匹配对象列表，跳过这次比对
					if(objects == null || objects.length == 0){
						logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode()  + "]的对象列表无法解析:" + privilege.getObjectList());
						continue;
					}
					//如果匹配对象的第一个是*，表示匹配所有
					if(objects[0].equals("*")){
						logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的对象列表第一个为*，查看是否需要进行属性权限匹配:" + privilege.getObjectAttributePattern());
						//判断属性权限，如果权限的属性模式为空，那么直接返回成功
						if(privilege.getObjectAttributePattern() == null || privilege.getObjectAttributePattern().equals("") || privilege.getObjectAttributePattern().equals("*")){
							logger.info("权限[" + privilege.getId() + "/" + privilege.getPrivilegeName() + "/" + privilege.getOperateCode() + "]的属性匹配规则为空或*，直接返回*");
							return "*";
						}						
					}
					validObjectIdList.append(',').append(privilege.getObjectList());
				}

			}
		}
		String result = validObjectIdList.toString().replaceFirst(",","");
		logger.info("用户[" + user.getUuid() + "针对类型[" + objectTypeCode + "]的操作" + operateCode + "的合法对象列表是:" + result);
		return result;
	}


	@Override
	public boolean isSuperPartner(User partner) {
		if(partner == null){
			return false;
		}
		if(partner.getUuid() == 300001 && partner.getUsername().equals("sa")){
			return true;
		}
		return false;
	}

	/**
	 * 该用户是否为平台级别的用户
	 * 如果不是，则只能查询该用户及其子账户的数据
	 * 如果是，可以查询所有ownerId一致的数据
	 * @param partner
	 * @return
	 */
	@Override
	public boolean isPlatformGenericPartner(User partner) {
		Assert.notNull(partner,"尝试检查是否为平台一般账户的对象为空");
		if(partner.getUserExtraTypeId() > 0){
			return false;
		}
		return true;

	}



	private User initCheck(CriteriaMap privilegeCriteria){
		if(privilegeCriteria == null || privilegeCriteria.size() < 1){
			logger.info("权限条件为空");
			return null;
		}
		long uuid = privilegeCriteria.getLongValue("uuid");
		int userTypeId = privilegeCriteria.getIntValue("userTypeId");
		if(uuid == 0){
			logger.info("权限条件中UUID=0");
			return null;
		}
		if(userTypeId == 0){
			logger.info("权限条件中用户类型未指定");
			return null;
		}
		if(StringUtils.isBlank(privilegeCriteria.getStringValue("objectTypeCode") )){
			logger.info("权限条件中对象类型ID和对象类型代码有一个为空");
			return null;
		}
		//查找用户
		User user = null;
		if(userTypeId == UserTypes.partner.id){
			user = partnerService.select( uuid );
			if(user == null){
				logger.info("指定的合作用户不存在[" + uuid + "]");
				return null;
			}
		}
		if(privilegeCriteria.getIntValue("userTypeId") == UserTypes.frontUser.id){
			return null;
		}
		if(user == null){
			logger.info("找不到指定的用户[" + uuid + "/" + userTypeId + "]");
			return null;
		}
		if(user.getRelatedRoleList() == null || user.getRelatedRoleList().size() < 1){
			logger.info("指定的用户[" + uuid + "/" + userTypeId + "]没有任何关联角色");
			return null;
		}
		return user;
	}

	/*
	 * @Override public boolean havePrivilege(HttpServletRequest request, String
	 * objectType, Object targetObject) {
	 * 
	 * return false; }
	 */






	@Override
	public boolean havePrivilege(User user, String targetObject, String code ) {
		CriteriaMap params = CriteriaMap.create();
		params.put("objectTypeCode",targetObject);
		params.put("operateCode",code);
		params.put("ownerId", user.getOwnerId());

		boolean result =  havePrivilege(user, params);
		logger.info("检查用户[" + user.getUuid() + "是否对对象[" + targetObject + "]具备操作:" + code + "的权限:" + result);
		return result;
	}

}
