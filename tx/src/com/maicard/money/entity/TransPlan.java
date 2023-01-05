package com.maicard.money.entity;


 import com.maicard.core.entity.BaseEntity;
 
public class TransPlan extends BaseEntity {

	private static final long serialVersionUID = 1L;

 
	private String transPlanName;

	private String transPlanDesc;

	private String processClass; //实现该交易方式的类名


	public TransPlan() {
	}
 

	public String getTransPlanName() {
		return transPlanName;
	}

	public void setTransPlanName(String transPlanName) {
		this.transPlanName = transPlanName;
	}

	public String getTransPlanDesc() {
		return transPlanDesc;
	}

	public void setTransPlanDesc(String transPlanDesc) {
		this.transPlanDesc = transPlanDesc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + id;

		return (int)result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TransPlan other = (TransPlan) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"transPlanId=" + "'" + id + "'" + 
			")";
	}

	public String getProcessClass() {
		return processClass;
	}

	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}


	
}
