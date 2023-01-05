package com.maicard.utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.maicard.core.constants.Operate;


public class OperateUtils {

	private static Set<String> operateEnum = ConcurrentHashMap.newKeySet();
	protected static final Logger logger = LoggerFactory.getLogger(OperateUtils.class);

	static {

		String fileUri = "classpath:pconfig/OperateEnum.dat";
		//读取允许的操作代码，默认为Operate枚举
		String operateStr = null;
		try {
			File file = ResourceUtils.getFile(fileUri);
			operateStr = FileUtils.readFileToString(file);

		} catch (IOException e) {
			logger.warn("操作码配置文件:{},不存在",fileUri);
		}
		int add = 0;
		if(operateStr != null){
			String[] ops = operateStr.split(",|;|\\s+");
			if(ops.length > 0){
				for(String op : ops){
					if(StringUtils.isNotBlank(op)){
						operateEnum.add(op.trim());
					}
				}
				add = ops.length;
			}
		}
		for(Operate o : Operate.values()){
			operateEnum.add(o.name());
		}
		logger.info("共加载{}个操作码，其中从文件读取的操作码{}个", operateEnum.size(), add);
	}

	public static Set<String> getOperateEnum() {
		return operateEnum;
	}


}
