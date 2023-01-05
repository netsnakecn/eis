package com.maicard.core.constants;

//输入级别
public enum InputLevel {
	none("none","无数据"),
	system("system","系统自动"),
	platform("platform","后台输入"),
	partner("partner","商户输入"),
	user("user","用户输入"),
	parent("parent","继承上级数据");

	public final String code;
	public final String name;
	private InputLevel(String code, String name){
		this.code = code;
		this.name = name;
	}
	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	@Override
	public String toString(){
		return this.code;
	}	
}
