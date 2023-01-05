package com.maicard.money.constants;

public enum SettlementStatus {
	unknown(0,"未知"),
	generated(730001,"已生成"),
	ackedByClient(730002,"客户已确认"),
	ackedByFinance(730003,"财务已确认"),
	submitToBank(730004,"已提交银行"),
	settled(730005,"已结算"),
	billed(730006,"已入账"),
	TEMPORARY(730007,"临时流转");

	public final int id;
	public final String name;
	private SettlementStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}	
	public SettlementStatus findById(int id){
		for(SettlementStatus value: SettlementStatus.values()){
			if(value.id == id){
				return value;
			}
		}
		return unknown;
	}
}


