package com.maicard.site.constants;



public enum NodeType{
	normal(1, "普通"),
	hiddenInPath(2,"不在路径中显示"),
	hiddenAll(3,"不可访问"),
	business(4,"业务节点"),
	navigation(5,"导航节点");

	public final int id;
	public final String name;
	private NodeType(int id, String name){
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
}