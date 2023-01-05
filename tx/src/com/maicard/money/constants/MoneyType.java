package com.maicard.money.constants;

//货币资金类型

public enum MoneyType {

			coin(103006,"coin", "金币"),
			point(103007,"point", "点数"),
			money(103009,"money","人民币"),	//人民币等值货币
			score(103010,"score", "积分");
			

			public final int id;
			public final String code;
			public final String name;
			private MoneyType(int id, String code, String name){
				this.id = id;
				this.code = code;
				this.name = name;
			}
			public int getId() {
				return id;
			}
			public String getCode() {
				return code;
			}
			public String getName() {
				return name;
			}	

			public MoneyType findByCode(String code){
				for(MoneyType value: MoneyType.values()){
					if(value.code.equals(code)){
						return value;
					}
				}
				return null;
			}
		}