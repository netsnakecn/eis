package com.maicard.tx.entity;


import com.maicard.core.entity.BaseEntity;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;

@NeedJmsDataSyncP2P
public class Invoice extends BaseEntity {

	private static final long serialVersionUID = 127950783664503807L;
	
	private int invoiceId;
	
	private long uuid;
	
	private float money;
	
	private String invoiceCode;	//发票号
	
	private String invoiceType;
	
	private String title;	//开票单位
	
	private String content;
	
	private String memory;		
	
	private String taxPayerId;		//纳税人识别号
	
	private String bankAccountId;		//银行帐号
	
	private String contactPhone;	//联系电话
	
	private String registeredAddress;	//注册地址
	

	public int getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(int invoiceId) {
		this.invoiceId = invoiceId;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTaxPayerId() {
		return taxPayerId;
	}

	public void setTaxPayerId(String taxPayerId) {
		this.taxPayerId = taxPayerId;
	}

	public String getBankAccountId() {
		return bankAccountId;
	}

	public void setBankAccountId(String bankAccountId) {
		this.bankAccountId = bankAccountId;
	}

	


	public String getInvoiceCode() {
		return invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getRegisteredAddress() {
		return registeredAddress;
	}

	public void setRegisteredAddress(String registeredAddress) {
		this.registeredAddress = registeredAddress;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float money) {
		this.money = money;
	}
 
	
	@Override
	public String toString(){
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"uuid=" + "'" + uuid + "'," + 
				"invoiceId=" + "'" + invoiceId + "'," + 
				"title=" + "'" + title + "'," + 
				"currentStatus=" + "'" + currentStatus + "'" + 
				")";
	}

}
