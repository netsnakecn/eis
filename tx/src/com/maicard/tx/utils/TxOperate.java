package com.maicard.tx.utils;


public enum TxOperate {
	settleUp(103001,"w","结算"), 
	post(103002,"w","提交"), 
	scanCode(102167,"w","扫码"),
	call(102168,"w","调用对应程序"),
	jump(102124,"w","跳转"), 
	iframe(102169,"w","在iframe中显示结果");
	
	
	public final int id;
	public  final String category;
	public final String name;

	private TxOperate(int id, String category, String name){
		this.id = id;
		this.category = category;
		this.name = name;
	}

	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}	
	public static TxOperate findByCode(String code){
		for(TxOperate value: TxOperate.values()){
			if(value.name().equalsIgnoreCase(code)){
				return value;
			}
		}
		return null;
	}
	public static TxOperate findById(int id){
		for(TxOperate value: TxOperate.values()){
			if(value.id == id){
				return value;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
