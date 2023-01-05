package com.maicard.core.constants;


public enum Operate {
		unknown(0,"","未知"),
		create(102001,"w","新增"),
		delete(102002,"w","删除"),
		update(102003,"w","更新"),
		get(102004,"r","查看"),
		relate(102005,"r","关联"),
		active(102109,"w","用户激活"),
		login(102110,"r","用户登入"),
		logout(102111,"w","用户退出"),
		jump(102124,"r","跳转"), 
		postProcess(102113,"w","后期处理"),
		close(102126,"w","关闭"),
		plus(102127,"w","增加"),
		minus(102128,"w","减少"),
		lock(102129,"w","锁定"),
		unlock(102130,"w","解锁"),
		preview(102135,"r","预览"),
		list(102139,"r","列表"), 
		JmsDataSync(102141,"w","更新本地数据"),
		flush(102142,"w","刷新数据"),
		download(102143,"s","下载"), 
		use(102147,"w","使用"),
		clear(102166,"w","清除所有关联数据"),
		refund(102170,"s","退款"), 
		notify(102173,"r","通知"),
		confirm(102176,"w","确认"),
		index(102177,"r","索引"),
		sensitive(102178,"s","敏感");	
		//ByPartner(102144,"ByPartner","按渠道"),
		//detailSelf(102145,"detailSelf","明细"),        
		//partner(102146,"partner","下级用户");
		public final int id;
		public  final String category;
		public final String name;
		
		private Operate(int id, String category, String name){
			this.id = id;
			this.category = category;
			this.name = name;
		}
		
		@Override
		public String toString(){
			return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
		}	
		public static Operate findByCode(String code){
			for(Operate value: Operate.values()){
				if(value.name().equalsIgnoreCase(code)){
					return value;
				}
			}
			return unknown;
		}
		public static Operate findById(int id){
			for(Operate value: Operate.values()){
				if(value.id == id){
					return value;
				}
			}
			return unknown;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}
