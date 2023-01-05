package com.maicard.security.constants;


//用户状态
public enum RoleTypes{
	
	ROLE_TYPE_DEPARTMENT(1),
	ROLE_TYPE_USER(2);

	public final int id;
	private RoleTypes(int id){
		this.id = id;
	}
	public int getId() {
		return id;
	}

	@Override
	public String toString(){
		return this.name() + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}	
	public RoleTypes findById(int id){
		for(RoleTypes value: RoleTypes.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return null;
	}
}
