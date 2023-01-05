package com.maicard.security.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.annotation.IgnoreJmsDataSync;
import com.maicard.mb.annotation.ProcessMessageObject;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.service.MessageService;
import com.maicard.security.dao.mapper.OperateLogMapper;
import com.maicard.security.entity.OperateLog;
import com.maicard.security.service.OperateLogService;

@Service
@ProcessMessageObject("operateLog")
public class OperateLogServiceImpl extends AbsBaseService<OperateLog,OperateLogMapper> implements OperateLogService,EisMessageListener {


	@Resource
	private ApplicationContextService applicationContextService;
	@Resource
	private ConfigService configService;
	@Resource
	private MessageService messageService;

	private boolean handlerOperateLog;
	private boolean handlerJmsDataSyncToLocal;
	private int keepOperateLogDay;
	private String oldOperateLogSaveDir;

	@PostConstruct
	public void init(){
		handlerOperateLog = configService.getBooleanValue("HANDLER_OPERATE_LOG",0);
		handlerJmsDataSyncToLocal = configService.getBooleanValue("HANDLER_JMS_DATA_SYNC_TO_LOCAL",0);
		keepOperateLogDay = configService.getIntValue("KEEP_OPERATE_LOG_DAY".toString(),0);
		if(keepOperateLogDay == 0){
			keepOperateLogDay = 30;
		}
		oldOperateLogSaveDir = configService.getValue("OLD_OPERATE_LOG_SAVE_DIR",0);

	}


	@Override
	@IgnoreJmsDataSync
	public int insert(OperateLog operateLog){
		logger.debug("操作日志：" + operateLog);
		
		boolean mqEnabled = configService.getBooleanValue(DataName.MQ_ENABLED.name(), operateLog.getOwnerId());
		
		if(!mqEnabled || handlerOperateLog){
			int rs = 0;
		
			try{
				rs = mapper.insert(operateLog);
			}catch(Exception e){
				e.printStackTrace();
			}
			if(rs == 1){
				return 1;
			} else {
				return 0;
			}
		}
		return _insertRemote(operateLog);
	}

	private int _insertRemote(OperateLog operateLog) {
		
		return 1;
	}




	@Override
	public void onMessage(EisMessage eisMessage) {
		if(!handlerOperateLog){
			logger.debug("本节点不负责处理业务操作日志数据更新，忽略消息[" + eisMessage.getMessageId() + "].");
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == 0){
			logger.debug("消息操作码为空，忽略消息[" + eisMessage.getMessageId() + "].");
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == Operate.create.id){
			try{
				operate(eisMessage);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			eisMessage = null;
		} else {			 
			logger.debug("消息操作码非法[" + eisMessage.getCode() + "]，忽略消息[" + eisMessage.getMessageId() + "].");
			eisMessage = null;
			return;
		}		
	}

	private void operate(EisMessage eisMessage) throws Exception{

		OperateLog operateLog = null;
		try{
			Object object = eisMessage.getExtra("operateLog");
			if(object == null){
				return;
			}
			if(object instanceof OperateLog){
				operateLog = (OperateLog)object;
			}
			if(object instanceof LinkedHashMap){
				ObjectMapper om = new ObjectMapper();
				om.setDateFormat(new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT));
				String textData = null;
				textData = om.writeValueAsString(object);
				operateLog = om.readValue(textData, OperateLog.class);
				if(operateLog == null){
					logger.warn("无法将请求执行的对象转换为OperateLog");
					return;
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}


		if(eisMessage.getCode() == Operate.create.id){
			if(handlerOperateLog){
				insert(operateLog);
				operateLog.setSyncFlag(0);
				messageService.sendJmsDataSyncMessage(null, "operateLogService", "insertLocal", operateLog);
				eisMessage = null;
				return;
			}
		} else {
			logger.debug("忽略操作[" + eisMessage.getCode() );
			eisMessage = null;
		}
	}


	@Scheduled(cron="0 */5 * * * ?")
	@Override
	public void cleanOldLog(){

		CriteriaMap params = CriteriaMap.create();
		params.put("endTime",DateUtils.addDays(new Date(), -keepOperateLogDay));

		if(handlerOperateLog){
			if(StringUtils.isNotBlank(oldOperateLogSaveDir)){
				//应当将清除的日志保存到指定目录
				List<OperateLog> operateLogList = null;
				try{
					operateLogList = mapper.list(params);
				}catch(Exception e){}
				if(operateLogList != null && operateLogList.size() >0){
					String fileName = oldOperateLogSaveDir + "/" + new SimpleDateFormat(Constants.STAT_HOUR_FORMAT).format(new Date());
					Date endTime = params.get("endTime");
					logger.info("尝试将[" + endTime + "]之前的日志保存到[" + fileName +  "],记录有" + operateLogList + "条");
					File destDir = new File(oldOperateLogSaveDir);
					if(!destDir.exists()){
						logger.debug("保存操作日志的路径不存在，创建目录" + oldOperateLogSaveDir);
						if(!destDir.mkdirs()){
							logger.error("无法创建目录[" + oldOperateLogSaveDir + "]");
						}
					}
					try{
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(fileName), "UTF-8"));
						StringBuffer sb = new StringBuffer();
						for(OperateLog operateLog : operateLogList){
							sb.append(operateLog.getObjectType());
							sb.append(",");
							sb.append(operateLog.getObjectId());
							sb.append(",");
							sb.append(operateLog.getUuid());
							sb.append(",");
							sb.append(operateLog.getOperateCode());
							sb.append(",");
							sb.append(operateLog.getOperateResult());
							sb.append(",");
							sb.append(operateLog.getData());
							sb.append(",");
							sb.append(operateLog.getIp());
							sb.append(",");
							sb.append(operateLog.getServerId());
							sb.append("\n");
						}
						bw.write(sb.toString());
						bw.close();
						sb = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		if(handlerJmsDataSyncToLocal || handlerOperateLog){
			//清除指定时间之前的日志			
			int rs = mapper.clearOldLog(params);
			Date endTime = params.get("endTime");
			logger.debug("清除[" + endTime + "]之前的操作日志,删除了[" + rs + "]条");
		}

	}



}
