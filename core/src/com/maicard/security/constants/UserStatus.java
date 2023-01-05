package com.maicard.security.constants;


//用户状态
public enum UserStatus{
	unknown(0,"未知"),
	normal(120001,"正常"),
	disabled(120002,"禁用"),
	locked(120003,"锁定"),
	unactive(120004,"未激活"),
	autoCreate(120005,"自动创建"),
	inQuery(120006,"查询中"),
	needQuery(120007,"需要查询"), 
	needSetPassword(120008,"需要设置密码"),
	authorized(120009,"已通过认证或授权"),
	hidden(120010,"隐藏");

	public final int id;
	public final String name;
	private UserStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}	
	public static UserStatus findById(int id){
		for(UserStatus value: UserStatus.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return null;
	}
}
