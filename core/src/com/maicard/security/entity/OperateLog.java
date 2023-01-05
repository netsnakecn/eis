package com.maicard.security.entity;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;

import com.maicard.core.constants.ObjectType;
import com.maicard.core.entity.BaseEntity;


/**
 * 系统操作日志
 *
 *
 * @author NetSnake
 * @date 2015年12月8日
 *
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class OperateLog extends BaseEntity{

	private static final long serialVersionUID = 1080202741945508270L;
	
	private long operateLogId;		//主键

	//private String objectType;

	private String objectId;

	private long uuid = 0;
	
	private String logVersion;

	private String operateCode;
	
	private String operateResult;

	private Date operateTime;
	
	private String value;
		
	private String methodType;
	
	private String requestMethod;
	
	private Date deadlineTime;
	
	private String ip;

	private String objectType;

	private int serverId;

	public OperateLog(){
	}
	
	public OperateLog(String objectType, String objectId, long uuid, String operate, String operateResult, String ip, int serverId, long ownerId){
		if(StringUtils.isBlank(objectType)) {
			this.objectType = ObjectType.unknown.name();
		} else {
			this.objectType = objectType;
		}
		this.objectId = objectId;
		this.uuid = uuid;
		this.operateCode = operate;
		this.operateResult = operateResult;
		this.operateTime = new Date();
		this.ip = ip;
		this.serverId = serverId;
		this.ownerId = ownerId;
	
	}
	


	public void setOperateCode(String operateCode) {
		this.operateCode = operateCode == null ? null : operateCode.trim();
	}


	

}