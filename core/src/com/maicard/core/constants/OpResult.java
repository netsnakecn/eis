package com.maicard.core.constants;

/**
 * 
 * 操作结果枚举
 *
 * @author GHOST
 * @date 2012-12-08
 */
public enum OpResult {
	/**
	 * 成功
	 */
	success(1),
	
	/**
	 * 接受
	 */
	accept(102005),
	
	/**
	 * 拒绝
	 */
	deny(102006),
	
	/**
	 * 失败
	 */
	failed(102007),
	
	
	
	/**
	 * 等待处理
	 */
	waiting(102009);

	public final int id;

	private OpResult(int id){
		this.id = id;
	}
	public int getId() {
		return id;
	}


	@Override
	public String toString(){
		return name();
	}
	public static OpResult findById(int id) {
		for(OpResult value: OpResult.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return null;
	}	
}
