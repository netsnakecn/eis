package com.maicard.money.constants;


//交易状态
	public enum TxStatus{
		unknown(0,"未知"),
		needProcess(710001,"需要处理"),
		accept(710002,"接受但还未成功"),
		//XXX marginMoneyNotEnough(710002;	//保证金不足;16;交易状态
		closed(710004,"已关闭"),//或未开放;16;交易状态
		auctionWaitingResult(710005,"等待竞拍结果"),
		auctionSuccess(710007,"竞拍成功"),
		success(710010,"交易成功"),
		failed(710011,"交易失败"),
		//completed()			= 710012;	//交易已处理;16;交易状态	
		inProcess(710013,"交易处理中"),
		timeout(710017,"交易超时"),
		inCart(710018,"暂存在购物车"),
		waitingPay(710019,"交易等待付款"),
		newOrder(710021,"新订单"),
		notOpen(710022,"未开放"),
		moneyUpdateFailed(710023,"资金更新失败"),
		systemException(710024,"系统异常"),
		release(710025,"需返回原状态"),
		waitingNotify(710026,"等待异步处理结果"),
		needNotProcess(710027,"不需要进行处理"),
		halfComplete(710028,"部分完成"),
		needReValidate(710029,"需再次验证"),
		forceClose(710030,"强行关闭"),
		successBiggerThanLabel(710031,"成功总额大于标签价格"), 
		forceSuccess(710032,"强制成功"),
		rollback(710033,"需要回滚之前的交易"),
		errorToSuccess(710034,"由失败转为成功"),
		validated(710037,"已完成所有验证"),
		needLastMatch(710038,"需要最后一次匹配"),
		waitingConfirm(710039,"等待验证结果"), 
		waitingOtherResult(710040,"等待其他返回结果"), 
		waitingMatch(710041,"等待匹配"), 
		forceMoveFrozenToRequest(710042,"强行将冻结金额改为请求金额"), 
		releaseAndFrozen(710044,"释放并暂停使用"),
		forceRegionMatch(710045,"只可用于同区域匹配"), 
		needBackupNodeProcess(710046,"需要备份节点处理"),
		deliveryConfirmed(710047,"已确认收货"),
		DELIVERY_ARRIVED(710048,"货物已到达"),
		bookingSuccess(710049,"预定成功"),
		delivering(710050,"发货中"),
		preDelivery(710051,"准备发货"),
		waitingComment(710052,"待评价"),
		commentClosed(710053,"评价已完成"), 
		refunding(710054,"退款中"),
		refunded(710055,"已退款"),
		waitingMinusMoney(710056,"等待扣款"),
		waitPostSuccess(710057,"已付款和扣款，执行结束操作"),
		REQUEST_REFUND(710058,"申请退款中");


	public final int id;
		public final String name;
		private TxStatus(int id, String name){
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
		public static TxStatus findById(int id){
			for(TxStatus value: TxStatus.values()){
				if(value.getId() == id){
					return value;
				}
			}
			return unknown;
		}
	}
