package com.maicard.site.constants;

import java.util.LinkedHashMap;
import java.util.Map;

 
//文档状态
public  enum DocumentStatus{
 	drift(130001,"草稿"), 
	published(130005,"已发布"),
	depAccept(130006,"待审批"),
	close(130007,"已关闭"), 
	deleted(130011,"已删除");

	public final int id;
	public final String desc;
	private DocumentStatus(int id, String desc){
		this.id = id;
		this.desc = desc;
	}
	public int getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}
	@Override
	public String toString(){
		return this.desc + "[" + this.id + "]" + "[" + name() + "]";
	}	
	public DocumentStatus findById(int id){
		for(DocumentStatus value: DocumentStatus.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return null;
	}
	
	public static Map<String,String> map() {
		Map<String,String> map = new LinkedHashMap<String,String>();
		for(DocumentStatus ot : DocumentStatus.values()) {
			map.put(String.valueOf(ot.id), ot.desc);
		}
		return map;
	}
	 
}