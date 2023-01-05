package com.maicard.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.constants.BasicStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;

import java.util.Date;




@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Relation extends BaseEntity {


    public static final String RELATION_LIMIT_UNIQUE = "unique";
	public static final String RELATION_LIMIT_GLOBAL_UNIQUE = "global_unique";
	public static final String RELATION_LIMIT_MULTI = "multi";


	private String fromType;


	private long fromId;

	private String toType;

	private long toId;



	/**
	 * 关联限制，如是否只能唯一关注
	 */
	private String relationLimit;


	/**
	 * 与该对象的关联类型，如一个文档的关联，可能是关注，也可能是阅读
	 */
	private String relationType;

	private long activity;



	public Relation(){}

	public Relation(String from, long fromId, String to, long toId){
		this.fromType = from;
		this.fromId = fromId;
		this.toType = to;
		this.toId = toId;
		this.currentStatus = BasicStatus.normal.id;
	}






}
