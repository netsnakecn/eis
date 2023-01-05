package com.maicard.security.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;

/**
 * 对用户密码的记录
 * 应以明文形式存放
 * 用于控制用户密码的使用期限和禁止重复使用之前密码等
 *
 *
 * @author NetSnake
 * @date 2015年12月27日
 *
 */
public class PasswordLog extends BaseEntity{

	private static final long serialVersionUID = 4334988100407731204L;

	private int passwordLogId;
	
	private long uuid;
	
	private String password;
	
	private Date createTime;

	public int getPasswordLogId() {
		return passwordLogId;
	}

	public void setPasswordLogId(int passwordLogId) {
		this.passwordLogId = passwordLogId;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
}
