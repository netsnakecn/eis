package com.maicard.core.entity;

import com.maicard.site.constants.SiteConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
@EqualsAndHashCode(callSuper = true)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "eis")
@Data
public  class IndexableEntity extends DeleteSafeEntity{



	@Field(type = FieldType.Text, analyzer = SiteConstants.CN_TEXT_ANALIZER)
	protected String code;

	@Field(type = FieldType.Text, analyzer = SiteConstants.CN_TEXT_ANALIZER)
	protected String title;

	@Field(type = FieldType.Text, analyzer = SiteConstants.CN_TEXT_ANALIZER)
	protected String brief;



	@Field(type = FieldType.Text, analyzer = SiteConstants.CN_TEXT_ANALIZER)
	protected String content;

	protected String objectType;

	/**
	 * 封面
	 */
	@Field(type = FieldType.Text)
	protected String cover;

	/**
	 * 头像
	 */
	@Field(type = FieldType.Text)
	protected String avatar;


	@GeoPointField
	protected GeoPoint location;

	public IndexableEntity(){}

	public void setTitle(String title) {
		if(title != null){
			this.title = title.trim();
		}
	}




	public void initLocation() {

	}


}
