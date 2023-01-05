package com.maicard.ws.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.maicard.core.entity.BaseEntity;
import com.maicard.security.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
/**
 * Eis Socket session,简称EsSession<br>
 * 包含web socket或nio socket
 * 
 *
 *
 * @author NetSnake
 * @date 2016年11月13日
 *
 */

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsSession extends BaseEntity {


	private String esSessionId;
	private String nativeSessionId;
	private String payload;
	private User user;
	private long ownerId;
	private int serverId;
	private String clientIp;
	
	private Date createTime;
	private Date lastSaveTime;
	

	
	public EsSession() {
		this.createTime = new Date();
	}
	
	public EsSession(long ownerId) {
		this.ownerId = ownerId;
		this.createTime = new Date();
	}
	
	public EsSession(String esSessionId, String nativeSessionId, long ownerId) {
		this.esSessionId = esSessionId;
		this.nativeSessionId = nativeSessionId;
		this.ownerId = ownerId;
		this.createTime = new Date();
	}


}
