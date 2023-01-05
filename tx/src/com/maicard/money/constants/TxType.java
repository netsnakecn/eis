package com.maicard.money.constants;


//交易类型

public enum TxType {
	unknown(0,"未知"),
	freeze(7,"冻结"),
	thaw(8,"解冻"),
	userMessage(9,"消息"),//非交易类型，用来生成用户消息的唯一ID messageId
	other(10,"其他交易"),
	pay(11,"付款"),
	buy(12,"消费"),
	sale(13,"出售"),
	match(14,"匹配"),
	stock(15,"库存"),
	withdraw(16,"提现"), 
	exchange(17,"兑换"),
	coupon(18,"优惠券"),
	booking(19,"预定"),
	refund(20,"退款"),
	charge(21,"充值");


	public final int id;
	public final String name;
	private TxType(int id, String name){
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return name();
	}
	@Override
	public String toString(){
		return this.name + "[" + this.id + "]" + "[" + this.getClass().getSimpleName() + "]";
	}	

	public TxType findById(int id){
		for(TxType value: TxType.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return unknown;
	}
}