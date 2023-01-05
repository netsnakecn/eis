package com.maicard.boss.controller.abs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.Constants;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

public class AbstractValidateController extends ValidateBaseController {


	@Resource
	protected ApplicationContextService applicationContextService;


	@PostConstruct
	public void init() {
		super.init();
		if(Container.userExtraTypes == null) {
			initUserExtraTypes();
		}

	}
	
	protected Map<Integer,String> getUserExtraTypes(){
		if(Container.userExtraTypes == null) {
			initUserExtraTypes();
		}
		return Container.userExtraTypes;
	}


	protected void initUserExtraTypes() {
		final String configName = "classpath:pconfig/user_extra_type.json";


		try {
			File file = ResourceUtils.getFile(configName);
			String text = FileUtils.readFileToString(file,Constants.DEFAULT_CHARSET);
			logger.info("读取配置文件:" + file.getName() + "=>" + text);
			Map<String,String> map = JsonUtils.getInstance().readValue(text, new TypeReference<Map<String,String>>(){});
			Container.userExtraTypes  = new HashMap<>();
			if(map.size() > 0){
				for(Entry<String,String>entry : map.entrySet()){
					Container.userExtraTypes.put(Integer.parseInt(entry.getKey().trim()), entry.getValue().trim());
				}
			}
		} catch (Exception e) {
			logger.error("无法读取或处理userExtraType配置文件:" + configName);
			e.printStackTrace();
		}

	}

	protected int getUserExtraIdByName(String name) {
		if(Container.userExtraTypes == null) {
			initUserExtraTypes();
		}
		if(Container.userExtraTypes == null) {
			logger.error("无法初始化用户扩展状态");
			return -1;
		}
		for(Entry<Integer,String> entry : Container.userExtraTypes.entrySet()) {
			if(entry.getValue().equalsIgnoreCase(name)) {
				return entry.getKey();
			}
		}
		return -1;

	}




}
