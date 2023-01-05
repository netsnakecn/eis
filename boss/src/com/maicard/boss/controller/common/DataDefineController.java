package com.maicard.boss.controller.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.maicard.base.CriteriaMap;
import com.maicard.base.Pair;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.DisplayLevel;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.InputLevel;
import com.maicard.core.constants.InputMethod;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.iface.TypeService;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.DataDefineService;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.security.entity.User;
import com.maicard.security.service.AuthorizeService;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.PartnerService;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.PagingUtils; 
/**
 * 扩展数据的配置 
 */
@Controller
@RequestMapping("/dataDefine")
public class DataDefineController extends ValidateBaseController{
	@Resource
	private ConfigService configService;
	@Resource
	private CertifyService certifyService;
	@Resource
	private AuthorizeService authorizeService;
	@Resource
	private PartnerService partnerService;
	@Resource
	private DataDefineService dataDefineService; 
	
	@Resource
	private ApplicationContextService applicationContextService;

	private static String[] validObjectTypes = null;
	private static final String[] dataTypes = {"String","int","boolean","float"};
	
	
	@PostConstruct
	public void init() {
		String value  = configService.getValue("VALID_OBJECT_TYPE", 0);
		if(StringUtils.isBlank(value)) {
			logger.warn("系统未定义合法的对象列表:validObjectTypes");
		} else {
			try {
				validObjectTypes = JsonUtils.getInstance().readValue(value, String[].class);
			} catch (IOException e) {
 				e.printStackTrace();
			}
		}
		logger.debug("系统定义的合法对象列表是:{}", value);
	}


	static Map<String,TypeService> typeServiceMap = null;

	@RequestMapping(method = RequestMethod.GET)
	public String list(HttpServletRequest request,HttpServletResponse response, ModelMap map, @RequestParam Map<String, Object> params )
			throws Exception {
		final String view = "common/dataDefine/index";
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		}

		CriteriaMap criteria =  CriteriaMap.create(params);

		criteria.put("ownerId", partner.getOwnerId());

		map.put("operateMap",getOperateMap(partner, ObjectType.dataDefine.name()));

		int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);		
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		int totalRows = dataDefineService.count(criteria);
		if(totalRows < 1){
			logger.debug("当前返回的数据数量是0");
			return view;
		}
		logger.debug("一共  " + totalRows + " 行数据，每页显示 " + rows + " 行数据，当前是第 " + page + " 页。");

		List<DataDefine> dataDefineList = dataDefineService.listOnPage(criteria);

		for(DataDefine dataDefine :  dataDefineList) {
			if(dataDefine.getExtra("objectIdName") == null) {
				//获取对象ID的具体名字
				String objectIdName = getObjectIdName(dataDefine);				
				dataDefine.setExtra("objectIdName",objectIdName);
			}
		}
		

		map.put("objectType", validObjectTypes);
		map.put("displayLevel", DisplayLevel.values());
		map.put("inputLevel", InputLevel.values());
		map.put("statusList", BasicStatus.values());
		map.put("rows", dataDefineList);
		map.put("dataType", dataTypes);

		//计算并放入分页
		map.put("contentPaging", PagingUtils.generateContentPaging(totalRows, rows, page));
		return view;
	}

	private String getObjectIdName(DataDefine dataDefine) {
		if(typeServiceMap == null || typeServiceMap.size() < 1) {
			initTypeServiceMap();
		}
		TypeService ts = typeServiceMap.get(dataDefine.getObjectType());
		if(ts == null) {
			return null;
		}
		
		List<Pair> list = ts.typeList(CriteriaMap.create());
		for(Pair p : list) {
			if(NumericUtils.parseLong(p.key) == dataDefine.getObjectId()) {
				return p.value.toString();
			}
		}
		return null;
	}

	private void initTypeServiceMap() {
		
		String[] beanNames = applicationContextService.getBeanNamesForType(TypeService.class);
		if(beanNames == null || beanNames.length < 1) {
			logger.warn("系统中没有任何TypeService");
		} else {
			typeServiceMap = new HashMap<String,TypeService>();
			for(String beanName : beanNames) {
				TypeService ts = applicationContextService.getBeanGeneric(beanName);
				typeServiceMap.put(ts.getObjectTypeName(),ts);
			}
			logger.debug("初始化了{}个TypeService");
		}
		
	}

	@RequestMapping(value="/get" + "/{id}")		
	public String get(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@PathVariable("id") int id) throws Exception {
		final String view = "common/dataDefine/detail";
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		}

		DataDefine dataDefine = dataDefineService.select(id);
		if (dataDefine==null) {
			throw new EisException(EisError.OBJECT_IS_NULL.id,"找不到ID=" + id + "的dataDefine对象");
		}
		List<Integer> statusList = new ArrayList<>();
		for (BasicStatus basicStatus : BasicStatus.values()) {
			statusList.add(basicStatus.id);
		}
		List<String> inputLevelList = new ArrayList<>();
		for (InputLevel inputLevel : InputLevel.values()) {
			inputLevelList.add(inputLevel.name());
		} 

		map.put("operateMap",getOperateMap(partner, ObjectType.dataDefine.name()));

		map.put("objectType", validObjectTypes);
		map.put("displayLevel", DisplayLevel.values());
		map.put("inputLevel", InputLevel.values());
		map.put("statusList", BasicStatus.values());
		map.put("dataDefine", dataDefine);
		map.put("dataType", dataTypes);
		return view;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(HttpServletRequest request,
			HttpServletResponse response, ModelMap map, DataDefine dataDefine)
					throws Exception {
		final String view = ViewNames.partnerMessageView;
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		}



		DataDefine oldDataDefine = dataDefineService.select(dataDefine.getId());
		if (oldDataDefine == null) {
			throw new EisException(EisError.OBJECT_IS_NULL.id,"找不到ID=" + dataDefine.getId() + "的dataDefine对象");
		}
		if (StringUtils.isNotBlank(dataDefine.getDataCode())) {
			oldDataDefine.setDataCode(dataDefine.getDataCode());
		}
		if (StringUtils.isNotBlank(dataDefine.getDataType())) {
			oldDataDefine.setDataType(dataDefine.getDataType());
		}
		if (StringUtils.isNotBlank(dataDefine.getDataName())) {
			oldDataDefine.setDataName(dataDefine.getDataName());
		}
		if (StringUtils.isNotBlank(dataDefine.getDataDescription())) {
			oldDataDefine.setDataDescription(dataDefine.getDataDescription());
		}
		if (StringUtils.isNotBlank(dataDefine.getDisplayLevel())) {
			oldDataDefine.setDisplayLevel(dataDefine.getDisplayLevel());
		}
		if (StringUtils.isNotBlank(dataDefine.getInputMethod())) {
			oldDataDefine.setInputMethod(dataDefine.getInputMethod());
		}
		if (StringUtils.isNotBlank(dataDefine.getInputLevel())) {
			oldDataDefine.setInputLevel(dataDefine.getInputLevel());
		}
		if (StringUtils.isNotBlank(dataDefine.getObjectType())) {
			oldDataDefine.setObjectType(dataDefine.getObjectType());
		}
		if (dataDefine.getObjectId()>0) {
			oldDataDefine.setObjectId(dataDefine.getObjectId());
		}
		if (StringUtils.isNotBlank(dataDefine.getCompareMode())) {
			oldDataDefine.setCompareMode(dataDefine.getCompareMode());
		}
		if (dataDefine.getCurrentStatus()>0) {
			oldDataDefine.setCurrentStatus(dataDefine.getCurrentStatus());
		}
		oldDataDefine.setSyncFlag(0);
		oldDataDefine.incrVersion();
		if (dataDefineService.update(oldDataDefine)>0) {
			map.put("message", EisMessage.success(OpResult.success.getId(), "操作完成"));
		}else {
			map.put("message", EisMessage.error(OpResult.failed.getId(), "操作失败"));
		}
		return view;
	}

	@RequestMapping(value="/create", method=RequestMethod.GET)	
	public String getCreate(HttpServletRequest request, HttpServletResponse response,ModelMap map) throws Exception {

		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);


		if (!isPlatformGenericPartner) {
			map.put("message", new EisMessage(OpResult.failed.id,EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		}
		map.put("objectType", validObjectTypes);
		map.put("displayLevel", DisplayLevel.values());
		map.put("inputLevel", InputLevel.values());
		map.put("statusList", BasicStatus.values());
		map.put("dataDefine", new DataDefine());
		map.put("dataType", dataTypes);
		return "common/dataDefine/create";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(HttpServletRequest request,
			HttpServletResponse response, ModelMap map,
			DataDefine dataDefine) throws Exception {
		final String view = ViewNames.partnerMessageView;
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		} 
		dataDefine.setOwnerId(partner.getOwnerId());
		
		if(dataDefine.getInputMethod() == null) {
			dataDefine.setInputMethod(InputMethod.input.name());
		}
		if(dataDefine.getInputMethod().startsWith("select")) {
			int offset = dataDefine.getInputMethod().indexOf(":");
			if(offset <= 0) {
				logger.error("新增字典的输入方式是select,但没有提交选项", JSON.toJSONString(dataDefine));
				map.put("message", EisMessage.error(EisError.PARAMETER_ERROR.id, "请输入可选项数据"));
				return ViewNames.partnerMessageView;			
			}
			String json = dataDefine.getInputMethod().substring(offset + 1);
			try {
				Map<String,String> validEnum = JsonUtils.getInstance().readValue(json, new TypeReference<Map<String,String>>(){});
				dataDefine.setValidDataEnum(validEnum);
			}catch(Exception e) {
				e.printStackTrace();
				map.put("message", EisMessage.error(EisError.PARAMETER_ERROR.id, "无法解析可选项数据:" + json));
				return ViewNames.partnerMessageView;			
			}
			dataDefine.setInputMethod(InputMethod.select.name());
			
		}
		if (dataDefineService.insert(dataDefine)>0) {
			map.put("message", EisMessage.success(OpResult.success.getId(), "操作完成"));
		}else {
			map.put("message", EisMessage.error(OpResult.failed.getId(), "操作失败"));
		}
		return view;
	}
	
	@RequestMapping(value="/delete", method=RequestMethod.POST)		
	@AllowJsonOutput
	public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

		long id = ServletRequestUtils.getLongParameter(request, "id", 0);
		User partner = new User();
		boolean validateResult = loginValidate(request, response, map, partner);
		if (!validateResult) {
			return ViewNames.partnerMessageView;
		}
		boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);


		if (!isPlatformGenericPartner) {
			map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
			return ViewNames.partnerMessageView;
		}
		DataDefine dataDefine = dataDefineService.select(id);
		if(dataDefine == null){
			logger.warn("找不到要删除的数据字典，ID=" + id);
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到要删除的数据字典ID=" + id));
			return ViewNames.partnerMessageView;
		}
		if(dataDefine.getOwnerId() > 0 && dataDefine.getOwnerId() != partner.getOwnerId()){
			logger.warn("要删除的数据字典，ownerId[" + dataDefine.getOwnerId() + "]与系统会话中的ownerId不一致:" + id);
			map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到要删除的数据字典ID=" + id));
			return ViewNames.partnerMessageView;
		}
		try{
			if(dataDefineService.delete(id) > 0){

				map.put("message", EisMessage.success(OpResult.success.getId(), "成功删除数据字典:" + id));
				return ViewNames.partnerMessageView;
			}
		}catch(DataIntegrityViolationException forignKeyException ){
			String error  = " 无法删除数据字典[" + id + "]，因为与其他数据有关联.";
			logger.error(error);
			map.put("message", EisMessage.error(EisError.RELATION_DATA_CONFILECT.id,error));
			return ViewNames.partnerMessageView;
		}
		map.put("message", EisMessage.error(EisError.DATA_ERROR.id,"无法删除数据字典"));
		return ViewNames.partnerMessageView;
	}
}
