package com.maicard.core.constants;

/**
 * 	基本状态
 *
 *
 * @author NetSnake
 * @date 2015年12月13日
 *
 */
public enum BasicStatus {
	unknown(0,"未知"),
	normal(100001,"正常"),
	disable(100002,"禁用"),
	defaulted(100003,"默认"),
	readOnly(100004,"只读"),
	hidden(100005,"隐藏"),
	dynamic(100006,"动态"),
	deleted(100007,"已删除"), 
	special(100010,"特殊");

	public final int id;
	public final String name;
	private BasicStatus(int id, String name){
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";////得到类的简写名称
	}

	public static BasicStatus findById(int id){
		for(BasicStatus value: BasicStatus.values()){
			if(value.id == id){
				return value;
			}
		}
		return unknown;
	}//根据ID 找出值 例如返回normal

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

};
