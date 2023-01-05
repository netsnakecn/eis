package com.maicard.core.entity;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.maicard.core.constants.OpResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统消息
 * @author NetSnake
 * @date 2012-4-23
 */
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_EMPTY)
@Data
public class EisMessage extends BaseEntity{

	protected String messageId;

	public String title;
	public String content;
	public String message;

	public String objectType;


	public int code = 0;
	public int result = 0;
	public long timestamp = 		  new Date().getTime();




	public EisMessage(){
		//this.messageId = UUID.randomUUID().toString();
	}
	
	public EisMessage(int code){
		this.code = code;
	}
	
	public EisMessage(int result, int code, String message){
		this.result = result;
		this.code = code;
		this.message = message;
	}


	

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"operateCode=" + "'" + code + "'," + 
				"message=" + "'" + message + "'," + 
				")";
	}


	public static EisMessage create(int result, int code, String msg) {
		return new EisMessage(result, code, msg);
	}
	public static EisMessage success(){
		return new EisMessage(OpResult.success.id, OpResult.success.id, null);
	}
	public static EisMessage success(String msg) {
		return new EisMessage(OpResult.success.id, OpResult.success.id, msg);
	}
	public static EisMessage success(int id, String msg) {
		return new EisMessage(OpResult.success.id, id, msg);
	}

	public static EisMessage error(int code){
		return new EisMessage(OpResult.failed.id, code, null);
	}
	public static EisMessage error(int id, String msg) {
		return new EisMessage(OpResult.failed.id, id, msg);
	}

}
