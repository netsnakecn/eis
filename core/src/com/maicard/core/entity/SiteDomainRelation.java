package com.maicard.core.entity;

public class SiteDomainRelation extends BaseEntity{
	
	

	private int siteDomainRelationId;
	
	private String siteCode;
	
	private String domain;
	
	private String cookieDomain;		//Cookie的作用域
	
	
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getSiteDomainRelationId() {
		return siteDomainRelationId;
	}
	public void setSiteDomainRelationId(int siteDomainRelationId) {
		this.siteDomainRelationId = siteDomainRelationId;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"siteDomainRelationId=" + "'" + siteDomainRelationId + "'," + 
				"siteCode=" + "'" + siteCode + "'," + 
				"domain=" + "'" + domain + "'," + 
				"ownerId=" + "'" + ownerId + "'," + 
				"currentStatus=" + "'" + currentStatus + "'" + 
				")";
	}
	public String getCookieDomain() {
		return cookieDomain;
	}
	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}
	

}
