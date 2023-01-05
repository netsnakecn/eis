package com.maicard.site.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;
 
public class Captcha extends BaseEntity{

	private static final long serialVersionUID = -7662060918486819609L;
	
	private int captchaId;
	private String captchaCode;
	private String checksum;
	
	private Date createTime;
	private int extraStatus = 0;
	private String imageName;
	private byte[] image;
	private int length = 0;
	private String patchcaTypeCode;
	private long supplier = 0;
	private int ttl = 0;
	private String word;
	private String outOrderId;
	
	
	private String currentStatusName;
	private String extraStatusName;
	
	public Captcha(){
		this.createTime = new Date();
	}
	public Captcha(String imageName){
		this.createTime = new Date();
		this.imageName = imageName;
	}


	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getExtraStatus() {
		return extraStatus;
	}

	public void setExtraStatus(int extraStatus) {
		this.extraStatus = extraStatus;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getPatchcaTypeCode() {
		return patchcaTypeCode;
	}

	public void setPatchcaTypeCode(String patchcaTypeCode) {
		this.patchcaTypeCode = patchcaTypeCode;
	}

	public long getSupplier() {
		return supplier;
	}

	public void setSupplier(long supplier) {
		this.supplier = supplier;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getOutOrderId() {
		return outOrderId;
	}

	public void setOutOrderId(String outOrderId) {
		this.outOrderId = outOrderId;
	}

	public String getCurrentStatusName() {
		return currentStatusName;
	}

	public void setCurrentStatusName(String currentStatusName) {
		this.currentStatusName = currentStatusName;
	}

	public String getExtraStatusName() {
		return extraStatusName;
	}

	public void setExtraStatusName(String extraStatusName) {
		this.extraStatusName = extraStatusName;
	}
	public int getCaptchaId() {
		return captchaId;
	}
	public void setCaptchaId(int captchaId) {
		this.captchaId = captchaId;
	}
	public String getCaptchaCode() {
		return captchaCode;
	}
	public void setCaptchaCode(String captchaCode) {
		this.captchaCode = captchaCode;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	
	
	
	

}
