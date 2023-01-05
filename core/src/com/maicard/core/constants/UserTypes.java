package com.maicard.core.constants;


public enum UserTypes {
	sysUser(121001, "系统用户"),
	partner(121002,"合作伙伴"),
	frontUser(121003,"终端用户"),
	machine(121004,"终端节点");

	public final int id;
	public final String name;
	private UserTypes(int id, String name){
		this.id = id;
		this.name = name;
	}
	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}

	public UserTypes findById(int id){
		for(UserTypes ut: UserTypes.values()){
			if(ut.id == id){
				return ut;
			}
		}
		return null;
	}
}