package com.maicard.core.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.maicard.serializer.CacheValueDeserializer;
/**
 * 存放在缓存系统中（如REDIS）的对象包装<br/>
 * 主要用于那些不支持超时的缓存系统，如REDIS中的Hash表对象<br/>
 * 对于普通REDIS键值，不需要进行包装
 *
 *
 * @author NetSnake
 * @date 2017年2月1日
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CacheValueDeserializer.class) 
public class CacheValue implements Serializable {
	public CacheValue(){}
	
	public CacheValue(String key, Object value, Date expireTime) {
		this.key = key;
		this.value = value;
		this.expireTime = expireTime;
		this.objectType = value.getClass().getName();
	}

	public String key;
	public Object value;
	public Date expireTime;
	public String objectType;
	
	public boolean isExpired() {
		if(this.expireTime == null){
			return false;
		}
		if(new Date().after(this.expireTime)){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T>T getValue(){
		return (T)value;
	}

}
