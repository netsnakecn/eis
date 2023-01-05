package com.maicard.boss.controller.abs;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.security.annotation.RequestPrivilege;
import com.maicard.utils.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.InputLevel;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.entity.ExtraData;
import com.maicard.core.service.CacheService;
import com.maicard.core.service.DataDefineService;
import com.maicard.mb.service.MessageService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRoleRelation;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerMenuRoleRelationService;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerRoleService;
import com.maicard.security.service.PartnerService;
import com.maicard.security.service.UserExtraTypeService;
import com.maicard.utils.ClassUtils;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.PagingUtils;

import static com.maicard.core.constants.ViewNames.partnerMessageView;

/**
 * 管理当前合作伙伴的子账户
 * 
 * 
 * @author NetSnake
 * @date 2012-6-10
 */

@Controller
public class AbstractPartnerController extends ValidateBaseController {
	@Resource
	protected AuthorizeService authorizeService;
	@Resource
	protected MessageService messageService;
	@Resource
	protected CertifyService certifyService;
	@Resource
	protected DataDefineService dataDefineService;
	@Resource
	protected PartnerService partnerService;	
	@Resource
	protected PartnerRoleService partnerRoleService;
	@Resource
	protected PartnerRoleRelationService partnerRoleRelationService;
	@Resource
	protected CacheService cacheService;
	
	@Autowired(required=false)
	protected UserExtraTypeService userExtraTypeService;

	@Resource
	protected PartnerMenuRoleRelationService partnerMenuRoleRelationService;


	@PostConstruct
	public void init() {
		super.init();
	}
	//仅页面
	@RequestMapping(method = RequestMethod.GET)
	public String page(HttpServletRequest request, HttpServletResponse response, ModelMap map,
					   @RequestParam Map<String, Object> params) throws Exception {
		final String view = getView(request) + "/index";

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		CriteriaMap partnerCriteria =  CriteriaMap.create(params);

		if(!isPlatformGenericPartner){
			//partnerService.setSubPartner(partnerCriteria, partner);
			map.put("message",EisMessage.error(EisError.ACCESS_DENY.id, "请先登录"));
			return partnerMessageView;
		}

		partnerCriteria.put("userExtraTypeIds", getUserExtraTypes());
		map.put("operateMap",getOperateMap(partner, "partner"));

		map.put("statusList", UserStatus.values());


		return view;
	}

	protected String getView(HttpServletRequest request) {
		int userTypeId = ServletRequestUtils.getIntParameter(request,"userTypeId",0);
		if(userTypeId == 1){
			return "biz/community";
		}

		return "biz/partner";
	}


	//列出当前用户的下级经销商
	@RequestMapping(value="/index",method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpServletResponse response, ModelMap map, 
			@RequestParam Map<String, Object> params) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		CriteriaMap partnerCriteria =  CriteriaMap.create(params);

		if(!isPlatformGenericPartner){
			//partnerService.setSubPartner(partnerCriteria, partner);
			map.put("message",EisMessage.error(EisError.ACCESS_DENY.id, "请先登录"));
			return partnerMessageView;
		}

		//只查询内部用户
		partnerCriteria.put("userExtraTypeIds", getUserExtraTypes());
		map.put("operateMap",getOperateMap(partner, "partner"));

		//map.put("userExtraTypeList", getUserExtraTypes());
		map.put("statusList", UserStatus.values());

		int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);		
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		 
		partnerCriteria.put("ownerId", partner.getOwnerId());
		//partnerCriteria.put("userTypeId",UserTypes.partner.id);
		
		
		int totalRows = partnerService.count(partnerCriteria);
		if(totalRows < 1){
			logger.debug("当前返回的数据数量是0");
			return partnerMessageView;
		}
		List<User> partnerList = partnerService.listOnPage(partnerCriteria);

		//计算并放入分页
		map.put("contentPaging", PagingUtils.generateContentPaging(totalRows, rows, page));
		map.put("total", totalRows);
		map.put("rows",partnerList);
		return partnerMessageView;
	}

	protected int[] getUserExtraTypes() {
		return new int[] {0};
	}


	@RequestMapping(value="/get" + "/{uuid}")		
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@PathVariable("uuid") long uuid) throws Exception {
		final String view = "biz/partner/detail";
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		//////////////////////// 结束标准流程 ///////////////////////
		if(!isPlatformGenericPartner){
			//不是内部用户，检查是不是访问自己或下级账户
			if(partner.getUuid() == uuid || partnerService.isValidSubUser(partner.getUuid(), uuid)){
				//用户合法访问
			} else {
				map.put("message", new EisMessage(OpResult.failed.id,EisError.SYSTEM_DATA_ERROR.getId(), "系统异常"));
				return partnerMessageView;
			}
		}



		User selectUser = partnerService.select(uuid);



		User clone = selectUser.clone();
		/*Map<String,ExtraData> extraDataMap = userDataService.combineExtraData(selectUser, DisplayLevel.partner.name());


		//保证扩展数据是3的倍数，以便前端布局
		int paddingCnt = extraDataMap.size() % 3;
		if(paddingCnt > 0) {
			paddingCnt = 3 - paddingCnt;
			logger.info("扩展数据数量是:{}，需要填充{}个空数据", extraDataMap.size(), paddingCnt);
			for(int i = 0; i < paddingCnt; i++) {
				//补几个空对象
				ExtraData ed = new ExtraData();
				String paddingName = "_padding_" ;
				ed.setDataCode(paddingName + i);
				ed.setDataType(paddingName);
				extraDataMap.put(ed.getDataCode(), ed);
			}
		}
		clone.setUserData(extraDataMap); */
		map.put("partner", clone);
		map.put("operateMap", getOperateMap(partner, ObjectType.partner.name()));
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());

		List<Role> partnerRoleList = partnerRoleService.list(criteria);
		List<Role> roleList = new ArrayList<Role>(partnerRoleList.size());
		if(clone.getRelatedRoleList() != null && clone.getRelatedRoleList().size() > 0) {
			for(Role role : partnerRoleList) {
				Role r2 = role.clone();
				for(Role existRole : clone.getRelatedRoleList()) {
					if(role.getId() == existRole.getId()) {
						r2.setCurrentStatus(BasicStatus.defaulted.id);
						break;
					}
				}
				roleList.add(r2);

			}
		}	
		criteria.init();
		List<Menu> menuList = partnerMenuRoleRelationService.listAllByPartner(partner); 

		map.put("roleList", roleList);
		map.put("menuList", menuList);
		map.put("statusList", UserStatus.values());
		//map.put("userExtraTypeList", getUserExtraTypes());
		return view;
	}







	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String getCreate(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}
		//	boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		CriteriaMap criteriaMap = CriteriaMap.create(partner.getOwnerId());
		if(!isPlatformGenericPartner) {
			//不是内部用户，则只能选择与自己相一致的类型的角色
			criteriaMap.put("roleLevel",partner.getUserExtraTypeId());
		}	
		if(userExtraTypeService == null) {
			logger.error("系统未注入userExtraTypeService");
		} else {
			Map<Long, String> extraTypeMap = userExtraTypeService.getUserExtraTypes(criteriaMap);
			logger.info("当前准备新建系统用户的可用类型是:{}",JSON.toJSONString(extraTypeMap));
			map.put("userExtraTypeMap",extraTypeMap);
		}
		User child = new User();
		child.setOwnerId(partner.getOwnerId());
		child.setUserTypeId(partner.getUserTypeId());
		Map<String,ExtraData> extraDataMap = null;// userDataService.combineExtraData(child, DisplayLevel.partner.name());
		//保证扩展数据是3的倍数，以便前端布局
		int paddingCnt = extraDataMap.size() % 3;
		if(paddingCnt > 0) {
			paddingCnt = 3 - paddingCnt;
			logger.info("扩展数据数量是:{}，需要填充{}个空数据", extraDataMap.size(), paddingCnt);
			for(int i = 0; i < paddingCnt; i++) {
				//补几个空对象
				ExtraData ed = new ExtraData();
				String paddingName = "_padding_" ;
				ed.setDataCode(paddingName + i);
				ed.setDataType(paddingName);
				extraDataMap.put(ed.getDataCode(), ed);
			}
		}
	//	child.setUserData(extraDataMap);
		//logger.debug("准备新建商户，数据:{}", JSON.toJSONString(child));
		//		HashMap<String, Integer> userTypeMaps = new HashMap<String, Integer>();
		//		for (UserTypes userTypes : UserTypes.values()) {
		//			userTypeMaps.put(userTypes.getName(), userTypes.getId());
		//			logger.debug("枚举 : " + userTypes.getName() + "   " + userTypes.getId());
		//		}
		//		map.put("userTypes", userTypeMaps);
		map.put("statusCodeList", UserStatus.values());
		//map.put("userExtraTypeList", getUserExtraTypes());
		map.put("partner", child);
		

		return "biz/partner/create";
	}
	@RequestMapping(value="/create", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map, User child) throws Exception {
		final String view = partnerMessageView;
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}

		/*if(child.getUserExtraTypeId() < 1) {
 			logger.error("未提交用户扩展类型");
			map.put("message",EisMessage.error(EisError.REQUIRED_PARAMETER.id, "请提交必须的用户扩展类型"));
			return view;
		}*/
		child.setOwnerId(partner.getOwnerId());
		if(child.getUserTypeId() <= 0){
			child.setUserTypeId(UserTypes.partner.id);
		}
		child.setParentUuid(partner.getUuid());
		child.setInviter(partner.getUuid());
		if(StringUtils.isBlank(child.getUsername()) && child.getNickName() != null){
			child.setUsername(child.getNickName());
		}
		if(StringUtils.isBlank(child.getNickName()) && child.getUsername() != null){
			child.setNickName(child.getUsername());
		}
		if(StringUtils.isBlank(child.getUsername())) {
			map.put("message",EisMessage.error(EisError.PARAMETER_ERROR.getId()));
			return partnerMessageView;
		}

			String[] type1 = request.getParameterValues("roleId");
		List<Role> relatedRoleList = new ArrayList<Role>();

		if (type1!=null){

			for (int i = 0; i < type1.length; i++) {  
				Role role=new Role();
				role.setId(NumericUtils.parseLong(type1[i]));
				relatedRoleList.add(role);
			}   
			child.setRelatedRoleList(relatedRoleList);
		} 
		CriteriaMap criteriaMap = CriteriaMap.create(partner.getOwnerId());
		//		dataDefineCriteria.setObjectType(ObjectType.product.toString());
		criteriaMap.put("objectType",ObjectType.user.toString());
		criteriaMap.put("objectId",child.getUserTypeId());
		List<DataDefine> dataDefineList = dataDefineService.list(criteriaMap);
		if (dataDefineList == null || dataDefineList.size() == 0) {
			logger.info("当前账户类型[" + child.getUserTypeId() + "]没有自定义字段.");
		} else {
			logger.debug("当前账户类型有[" + dataDefineList.size() + "]个自定义数据规范");

			for (DataDefine dataDefine : dataDefineList) {
				logger.debug("尝试获取数据规范[" + dataDefine.getDataCode() + "/" + dataDefine.getId() + "]定义的数据");
				//String dataStr = ServletRequestUtils.getStringParameter(request, dataDefine.getDataCode());
				String dataStr = ServletRequestUtils.getStringParameter(request, Constants.DATA_KEY_PREFIX  + dataDefine.getDataCode());
				if (dataStr == null || dataStr.equals("")) {
					logger.debug("数据规范[" + dataDefine.getDataCode() + "/" + dataDefine.getId() + "]没有提交数据");
					continue;
				}
				dataStr = dataStr.trim();
				logger.debug("数据规范[" + dataDefine.getDataCode() + "/" + dataDefine.getId() + "]提交的数据是[" + dataStr + "]");

				ExtraData userData = new ExtraData();
				userData.setDataDefineId(dataDefine.getId());
				userData.setDataCode(dataDefine.getDataCode());
				userData.setCurrentStatus(BasicStatus.normal.id);
				userData.setDataValue(dataStr);

				logger.debug("尝试插入自定义账户数据[" + userData.getDataCode() + "/" + userData.getDataDefineId() + "]，数据内容:[" + userData.getDataValue() + "]");
				//child.getUserData().put(userData.getDataCode(), userData);
			}
		}
		child.setRelatedRoleList(relatedRoleList);
		long headUuid = ServletRequestUtils.getLongParameter(request, "headUuid", 0);
		child.setHeadUuid(headUuid);
		//processPartnerData(request, child);
		logger.info("请求写入新的Partner[" + child.getUsername() + "],parentUuid=[" + child.getParentUuid() + "]");
		child.setCreateTime(new Date());
		EisMessage message = null;

		logger.debug(child.toString()+"****************************************************************************");
		logger.debug(child.getNickName()+child.getUsername()+"++++++++++++++"+child.getUserPassword()+"++++++++++++++"+child.getAuthKey()+"++++++++++++++");
		if(partnerService.insert(child) == 1 ){
			criteriaMap.init();
			criteriaMap.put("cacheName",CacheNames.cacheNameUser);
			criteriaMap.put("key", "Machine#"+child.getUuid());
			logger.info("删除缓存:{}", JSON.toJSONString(criteriaMap));
			cacheService.delete(criteriaMap);
			message = new EisMessage(OpResult.success.id, OpResult.success.getId(),"操作完成");		
		} else {
			message = new EisMessage(OpResult.failed.id, OpResult.failed.getId(),"操作失败");
		}

		map.put("message", message);
		return view;
	}

	@RequestMapping(value="/listForSelect", method=RequestMethod.GET)
	@AllowJsonOutput
	@RequestPrivilege(object="partner",operate="r")
	public String listForSelect(HttpServletRequest request, HttpServletResponse response, ModelMap map, @RequestParam Map<String,Object> params) throws Exception {
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		List<User>merchantList = null;

		CriteriaMap criteria = CriteriaMap.create(params);
		criteria.put("ownerId",partner.getOwnerId());


		int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);


		List<Integer> memberTypeList = new ArrayList<Integer>();
		boolean addBasic = false;
		String args = ServletRequestUtils.getStringParameter(request, "userTypeId");

		if(StringUtils.isNotBlank(args)) {

			String[] arr = args.split(",");
			for(String k : arr) {
				int type = NumericUtils.parseInt(k.trim());
				memberTypeList.add(type);

			}
		}

		if(memberTypeList.size() < 1) {
			logger.error("未提交要查询的商户类型:userTypeId");
			map.put("message",EisMessage.error(EisError.REQUIRED_PARAMETER.id,"请提交查询类型"));
			return ViewNames.partnerMessageView;
		}

		int[] userTypeIds = NumericUtils.list2Array(memberTypeList);

		//select2的模糊查询
		String q = ServletRequestUtils.getStringParameter(request, "q");
		if(StringUtils.isNotBlank(q)) {
			if(NumericUtils.isNumeric(q)) {
				criteria.put("fuzzyUuid", q.trim());
			} else {
				criteria.put("fuzzyName", q.trim());
			}
		}
		if(!isPlatformGenericPartner) {
			//partnerService.setSubCriteria(criteria,partner,true);
		}

		criteria.putArray("userTypeIds", userTypeIds);

		int totalRows = partnerService.count(criteria);
		map.put("total", totalRows);
		if(totalRows < 1){
			logger.debug("当前返回的数据数量是0");
			return ViewNames.partnerMessageView;
		}
		logger.debug("一共  " + totalRows + " 行数据，每页显示 " + rows + " 行数据，当前是第 " + page + " 页。");
		PagingUtils.paging(criteria, rows, page);

		merchantList = partnerService.listSimplePartner(criteria);
		logger.debug("为选择查询商户的条件是:{}", JsonUtils.toStringFull(criteria));
		List<Map<String,Object>> dataMap = new ArrayList<Map<String,Object>>();
		for(User p : merchantList) {
			Map<String,Object>data = new HashMap<String,Object>();
			data.put("id", p.getUuid());
			data.put("text", p.getUuid() + "-" + p.getNickName());
			dataMap.add(data);
		}
		map.put("rows",  dataMap);

		return ViewNames.partnerMessageView;
	}


	@RequestMapping(value="/update", method=RequestMethod.POST)	
	@AllowJsonOutput
	public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map, 
			User child) throws Exception {
		final String view = partnerMessageView;
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return view;
		}

		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);
		if(!isPlatformGenericPartner){
			map.put("message", new EisMessage(OpResult.failed.id,EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return view;
		}
		//String modelType = request.getRequestURI().split("/")[1].replaceAll("\\.\\w*$", "");

		User _oldChild = partnerService.select(child.getUuid());
		if(_oldChild == null) {
			logger.error("找不到要更新的节点UUID={}", child.getUuid());
			map.put("message", new EisMessage(OpResult.failed.id,EisError.DATA_ERROR.id, "找不到要更新的machine uuid=" + child.getUuid()));
			return view;
		}
		String dataMode = ServletRequestUtils.getStringParameter(request, "dataMode");
		logger.info("当前更新模式是:{}，是否为单项数据更新:{}", dataMode, StringUtils.isBlank(dataMode));
		if(StringUtils.isBlank(dataMode)){
			//仅更新单个属性，可能是原生属性或扩展属性
			String updateDataCode = ServletRequestUtils.getStringParameter(request,"extraData");
			String updateAttribute = ServletRequestUtils.getStringParameter(request,"nativeData");
			if (StringUtils.isNotBlank(updateDataCode)) {
				//扩展数据更新
				String dataName  = updateDataCode.replaceFirst("^" + Constants.DATA_KEY_PREFIX,"");
				logger.info("请求更新扩展属性:{}=>{}", updateDataCode, dataName);
				CriteriaMap dataDefineCriteria = CriteriaMap.create(partner.getOwnerId());
				dataDefineCriteria.put("objectType",ObjectType.user.name());
				dataDefineCriteria.put("objectId",UserTypes.partner.id);
				dataDefineCriteria.put("dataCode",dataName);
				DataDefine dataDefine = dataDefineService.selectOne(dataDefineCriteria);
				if(dataDefine == null){
					logger.error("找不到要更新的商户扩展数据定义:" + dataName);
					map.put("message", new EisMessage(OpResult.failed.id,EisError.DATA_ERROR.id, "找不到要更新的数据"+ dataName));
					return view;
				}
				if(dataDefine.getInputLevel() != null){
					if(dataDefine.getInputLevel().equals(InputLevel.system.toString())){
						logger.error("不允许更新system级别的扩展数据:" + dataName);
						map.put("message", new EisMessage(OpResult.failed.id,EisError.DATA_ERROR.id, "找不到要更新的数据"));
						return view;
					}
					if(!isPlatformGenericPartner && dataDefine.getInputLevel().equals(InputLevel.platform.toString())){
						logger.error("当前不是内部用户，不允许更新platform级别的扩展数据:" + dataName);
						map.put("message", new EisMessage(OpResult.failed.id, EisError.DATA_ERROR.id, "找不到要更新的数据"));
						return view;
					}
				}


				String dataValue = ServletRequestUtils.getStringParameter(request,updateDataCode);
				if(StringUtils.isBlank(dataValue)){
					logger.error("要求更新商户[" + child.getUuid() + "]的数据:" + dataName + "但是未提交数据的值，将视为删除该数据");
					ExtraData removeData = null;
					/*for(Map.Entry<String,ExtraData> entry : _oldChild.getUserData().entrySet()) {
						if(entry.getKey().equalsIgnoreCase(dataName)) {
							removeData = entry.getValue();
							break;
						}
					}*/
					if(removeData == null) {
						logger.error("找不到要删除的用户:{}的配置数据:{}", child.getUuid(),dataName);
						map.put("message", new EisMessage(OpResult.failed.id, EisError.DATA_ERROR.id, "找不到要更新的数据"));
						return partnerMessageView;
					}
					//userDataService.delete(removeData);
					partnerService.evictCache(_oldChild);

					map.put("message",EisMessage.success("成功删除扩展属性:" + dataName));
					return view;
				}
				ExtraData userData = null;
				//if(_oldChild.getUserData() == null){
				//	_oldChild.initUserData();
				//}
				/*userData = _oldChild.getUserData().get(dataName);

				if(userData == null){
					userData = new ExtraData(dataDefine);
					userData.setUuid(child.getUuid());
					userData.setDataValue(dataValue);
					userData.setObjectId(child.getUuid());
					logger.warn("在被修改的商户[" + child.getUuid() + "]中没有要更新的属性:" + dataName + ",准备新增:" + JSON.toJSONString(userData));
					int rs = 0;//userDataService.insert(userData);
					if(rs == 1){
						map.put("message", EisMessage.success(OpResult.success.id, "3更新属性:" + dataName + "成功"));
						partnerService.evictCache(_oldChild);
						//_oldChild.setExtraData(dataName, dataValue);
						messageService.sendJmsDataSyncMessage(null, "userDataService", "insert", userData);
					} else {
						map.put("message", EisMessage.success(OpResult.success.id, "3更新属性:" + dataName + "失败"));

					}
				} else {
					userData.setDataValue(dataValue);
					int rs = userDataService.update(userData);
					if(rs == 1){
						map.put("message", EisMessage.success(OpResult.success.id, "2更新属性:" + dataName + "成功"));
						_oldChild.setExtraData(dataName, dataValue);
						partnerService.evictCache(_oldChild);
						messageService.sendJmsDataSyncMessage(null, "userDataService", "update", userData);
					} else {
						map.put("message", EisMessage.success(OpResult.success.id, "2更新属性:" + dataName + "失败"));
					}
				}*/
				map.put("message", EisMessage.success(OpResult.success.id, "2更新属性:" + dataName + "成功"));
				return view;

			} else if (StringUtils.isNotBlank(updateAttribute)) {
				//可能是更新原生属性
				String attributeName  = updateAttribute.replaceFirst("^" + Constants.NATIVE_KEY_PREFIX,"");
				logger.info("请求更新原生属性:{}=>{}", updateAttribute, attributeName);
				String dataValue = ServletRequestUtils.getStringParameter(request,updateAttribute);
				if (StringUtils.isNotBlank(dataValue)) {
					dataValue = dataValue.trim();
					ClassUtils.setAttribute(_oldChild, attributeName, dataValue.trim(), Constants.COLUMN_TYPE_NATIVE);
					_oldChild.incrVersion(); 
					//_oldChild.setUserData(null);
					int rs = partnerService.update(_oldChild);
					logger.info("写入单独的原生属性:{}=>{},更新结果:{}", attributeName, dataValue, rs);
					if(rs == 1) {
						partnerService.evictCache(_oldChild);
						map.put("message", EisMessage.success(OpResult.success.id, "1更新属性:" + attributeName + "成功"));
					} else {
						map.put("message", EisMessage.error(OpResult.failed.id, "1更新属性:" + attributeName + "失败"));
					}
					return view;

				} else {

					logger.error("当前请求更新原生属性:{}但未提交数据值", attributeName);
					map.put("message", EisMessage.error(OpResult.failed.id, "更新属性" + attributeName + "失败，未提交数据值"));
					return view;
				} 
			} else {
				//未提交扩展或原生属性的更新，也不是表单全体更新
				logger.error("当前请求更新单个属性但未提交数据名或属性名");
				map.put("message", EisMessage.error(OpResult.failed.id, "请提交要更新的属性"));
				return view;
			} 

		}
		String roleIds=ServletRequestUtils.getStringParameter(request, "roleId");
		if (StringUtils.isNotBlank(roleIds)){
			String[] type1 = roleIds.split(",");

			CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
			criteria.put("uuid",child.getUuid());
			int rs = partnerRoleRelationService.deleteBy(criteria);
			logger.info("更新用户时，强制删除关联角色:{}", rs);
			for (int i = 0; i < type1.length; i++) {  
				UserRoleRelation partnerRoleRelation = new UserRoleRelation();
				partnerRoleRelation.setUuid(child.getUuid());
				partnerRoleRelation.setRoleId(Integer.parseInt(type1[i]));
				partnerRoleRelation.setCurrentStatus(BasicStatus.normal.id);
				partnerRoleRelation.setOwnerId(partner.getOwnerId());
				partnerRoleRelationService.insert(partnerRoleRelation );
			}   
		}



		long headUuid = ServletRequestUtils.getLongParameter(request, "headUuid", 0);
		child.setHeadUuid(headUuid);
		EisMessage message = null;
		logger.debug(child.toString()+"++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		if(partnerService.update(child) > 0){
			logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			messageService.sendJmsDataSyncMessage(null, "machineService", "insert", child);
			message = new EisMessage(OpResult.success.id,OpResult.success.id,"操作完成");		
		} else {
			message = new EisMessage(OpResult.failed.id,OpResult.failed.id,"操作失败");
		}

		map.put("message", message);		
		return view;
	}


	@RequestMapping(value="/delete", method=RequestMethod.POST)
	@AllowJsonOutput
	public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return partnerMessageView;
		}
		long uuid = ServletRequestUtils.getLongParameter(request, "uuid", 0);
		if(uuid <= 0) {
			logger.error("删除商户未提交uuid");

			map.put("message", new EisMessage(OpResult.failed.id, EisError.REQUIRED_PARAMETER.id,"请提交参数uuid"));
			return partnerMessageView;
		}
		int rs = partnerService.delete(uuid);
		logger.info("删除商户:{}结果:{}", uuid, rs);
		if(rs > 0) {
			map.put("message", new EisMessage(OpResult.success.id,OpResult.success.id,"成功删除"));		
		} else {
			map.put("message", new EisMessage(OpResult.failed.id, EisError.DATA_UPDATE_FAIL.id,"删除失败"));		

		}
		return partnerMessageView;
	}


}
