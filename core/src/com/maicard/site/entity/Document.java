package com.maicard.site.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.constants.Constants;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.site.annotation.InputLevel;
import com.maicard.views.JsonFilterView;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Document  extends IndexableEntity {

	@Serial
	private static final long serialVersionUID = -1L;

	@JsonIgnore
	public boolean isCacheable(){
		return true;
	}



	@InputLevel(JsonFilterView.Partner.class)
	private String author;

	@JsonView(JsonFilterView.Partner.class)
	private long publisherId;

	@JsonView(JsonFilterView.Partner.class)
	@JsonFormat(pattern = Constants.DEFAULT_TIME_FORMAT)
	private Date createTime;

	@JsonFormat(pattern = Constants.DEFAULT_TIME_FORMAT)
	private Date publishTime;

	@JsonView(JsonFilterView.Partner.class)
	@InputLevel(JsonFilterView.Partner.class)
	@JsonFormat(pattern = Constants.DEFAULT_TIME_FORMAT)
	private Date validTime;


	@InputLevel(JsonFilterView.Partner.class)
	private String tags;

	@JsonView(JsonFilterView.Partner.class)
	private int flag;

	@JsonView(JsonFilterView.Partner.class)
	private String language;


	@JsonView(JsonFilterView.Partner.class)
	private int displayWeight;

	private String displayType;
	 

	private int alwaysOnTop;

	@JsonView(JsonFilterView.Partner.class)
	@InputLevel(JsonFilterView.Partner.class)
	private String redirectTo;

	@JsonView(JsonFilterView.Partner.class)
	@InputLevel(JsonFilterView.Partner.class)
	private int templateId;

	@JsonView(JsonFilterView.Partner.class)
	private Date lastModified;
	
	/**
	 * 浏览该文档所需的级别
	 */
	@JsonView(JsonFilterView.Partner.class)
	private int viewLevel;

	/**
	 * 文档层级
	 */
	@JsonView(JsonFilterView.Partner.class)
	private int level;



	//statusName、languageName、documentTypeName、publisher等并不存在于document表中，而是查询自其他服务


	@JsonView(JsonFilterView.Partner.class)
	private Node defaultNode;

	@JsonView(JsonFilterView.Partner.class)
	private List<Node> relatedNodeList;




	private String publisher;

	//private String documentTypeName;
	
	private String viewUrl;

	@JsonView(JsonFilterView.Partner.class)
	private String type;


	private int pages;




	public Document() {
	}



	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"id=" + "'" + id + "'," + 
				"code=" + "'" + code + "'," +
				"type=" + "'" + type + "'" +
				"currentStatus=" + "'" + currentStatus + "'," + 
				"ownerId=" + "'" + ownerId + "'" + 

				")";
	}
	




	@Override
	public Document clone() {
		return (Document)super.clone();
	}




}
