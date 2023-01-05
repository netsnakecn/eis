package com.maicard.tx.service.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.base.ImplNameTranslate;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.core.constants.*;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.core.service.impl.ContextUtils;
import com.maicard.mb.constants.MbConstants;
import com.maicard.mb.constants.MessageBusEnum;
import com.maicard.mb.constants.SyncMode;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.entity.User;
import com.maicard.security.service.FrontUserService;
import com.maicard.site.entity.Node;
import com.maicard.tx.dao.mapper.OrderMapper;
import com.maicard.tx.entity.DeliveryOrder;
import com.maicard.tx.service.DeliveryOrderService;
import com.maicard.tx.service.StockService;
import com.maicard.utils.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalOrderIdService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.constants.MoneyType;
import com.maicard.money.constants.TxStatus;
import com.maicard.money.entity.Money;
import com.maicard.money.entity.Pay;
import com.maicard.money.entity.Price;
import com.maicard.money.iface.TxExecutor;
import com.maicard.money.service.PayService;
import com.maicard.money.service.PriceService;
import com.maicard.money.service.TransPlanService;
import com.maicard.tx.entity.Item;
import com.maicard.tx.entity.Order;
import com.maicard.tx.service.ItemService;
import com.maicard.tx.service.OrderService;

@Service
public class OrderServiceImpl extends AbsGlobalSyncService<Order, OrderMapper> implements OrderService {
 

    @Resource
    private ConfigService configService;

    @Resource
    private GlobalUniqueService globalUniqueService;

    @Resource
    private FrontUserService frontUserService;

    @Resource
    private MessageService messageService;
    @Resource
    private ItemService itemService;
    @Resource
    private PayService payService;
    @Resource
    private PriceService priceService;

    @Resource
    private StockService stockService;

    @Resource
    private TransPlanService transPlanService;

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private GlobalOrderIdService globalOrderIdService;

    private static boolean mqEnabled;

    private static boolean HANDLE_ORDER = false;

    @PostConstruct
    public void init() {
        mqEnabled = configService.getBooleanValue(DataName.MQ_ENABLED.name(), 0);
        HANDLE_ORDER = configService.getBoolProperty(HandlerEnum.HANDLE_ORDER.name());
        messageBusEnum = MessageBusEnum.TX;
    }

    @Override
    public Order add(Item item, boolean directBuy, String identify, int cartId) throws Exception {

        if (item == null) {
            logger.error("商品为空,无法添加购物车");
            return null;
        }
        if (item.getChargeFromAccount() <= 0) {
            logger.error("商品没有用户ID,无法添加购物车");
            return null;
        }
        if (StringUtils.isBlank(item.getTransactionId())) {
            item.setTransactionId(globalOrderIdService.generate(item.getTransactionTypeId()));
        }
        if (item.getCount() < 1) {
            item.setCount(1);
        }
        if (item.getEnterTime() == null) {
            item.setEnterTime(new Date());
        }
        String priceType = null;
        if (item.getPrice() != null) {
            priceType = item.getPrice().getPriceType();
        }
        Order order = null;
        if (cartId > 0) {
            order = select(cartId);
            if (order == null) {
                logger.error("找不到加入商品的指定购物车:" + cartId);
                return null;
            }
            if (order.getOrderType() != null && order.getOrderType().equals(Order.ORDER_TYPE_STORE)) {

                logger.error("准备加入商品的指定购物车:" + cartId + "是一个已存储的购物车，不能再放入商品");
                return null;
            }
            if (item.getSupplyPartnerId() > 0) {
                order.setInviter(item.getSupplyPartnerId());
            }
            //FIXME 已有购物车结算时添加新产品VIP
            //return cart;

        }
        //if(directBuy){
        order = getCurrentOrder(item.getChargeFromAccount(), priceType, Order.ORDER_TYPE_TEMP, identify, item.getOwnerId(), directBuy);
        //} else {
        //	order = getCurrentOrder(item.getChargeFromAccount(), priceType, Order.ORDER_TYPE_TEMP, identify, item.getOwnerId(), directBuy);
        //}
        if (order == null) {
            logger.error("在把交易[" + item.getTransactionId() + "]加入购物车时，无法获得当前购物车");
            return null;
        }
        logger.debug("得到了购物车:{}", order.getId());
        order.setTtl(item.getTtl());
        if (item.getSupplyPartnerId() > 0) {
            order.setInviter(item.getSupplyPartnerId());
        }
        item.setCartId(order.getId());
        order.setPrice(item.getPrice());
        order.getPrice().setMoney(order.getPrice().getMoney() * item.getCount());
        boolean needDelivery = item.getBooleanExtra(DataName.productNeedDelivery.name());
        logger.info("添加到购物车的商品:{}是否需要配送:{}", JSON.toJSONString(item), needDelivery);
        if (needDelivery) {
            order.setExtra(DataName.productNeedDelivery.name(), "true");
        }
        int totalProduct = 0;
        int totalGoods = 0;
        boolean goodsCountUpdated = false;
        if (!directBuy) {
            CriteriaMap itemCriteria = new CriteriaMap();
            itemCriteria.put("cartId", order.getId());
            itemCriteria.putArray("currentStatus", TxStatus.newOrder.getId());
            List<Item> inOrderItemList = itemService.list(itemCriteria);
            if (inOrderItemList != null && inOrderItemList.size() > 0) {
                for (Item i : inOrderItemList) {
                    totalProduct++;
                    if (i.getProductId() == item.getProductId()) {
                        logger.info("在购物车[" + order.getId() + "]找到了相同的产品[" + i.getProductId() + "]，仅增加其数量");
                        goodsCountUpdated = true;
                        i.setCount(i.getCount() + item.getCount());
						/*if(cart.getTotalProduct() < 1){
						cart.setTotalProduct(1);
					}*/
                        update(i, i.getCount());
                        logger.debug("仅增加购物车[" + order.getId() + "]中的商品[" + i.getProductId() + "]数量:" + i.getCount());
                    }
                    totalGoods += i.getCount();

                }
            }
        }
        if (goodsCountUpdated) {
            order.setTotalGoods(totalGoods);
            order.setTotalProduct(totalProduct);
            logger.debug("增加购物车[" + order.getId() + "]中的商品数量后，商品总数:" + order.getTotalProduct() + ",物品总数:" + order.getTotalGoods() + ",更新购物车");
            update(order);
            return order;
        }
        //对应购物车中没有任何商品，直接添加商品

        logger.info("对应购物车[" + order.getId() + "]中没有找到对应商品[" + item.getProductId() + "]，直接向数据库中添加商品");
        itemService.insert(item);

        order.setTotalGoods(totalGoods + 1);
        order.setGoodsDesc(order.getGoodsDesc() == null ? item.getName() : (order.getGoodsDesc() + "," + item.getName()));
        order.setTotalProduct(totalProduct + 1);
        Money m = Money.from(item.getPrice(), order.getUuid());
        if (order.getTotalMoney() <= 0) {
            order.setTotalMoney(m.getChargeMoney());
        } else {
            order.setTotalMoney(order.getTotalMoney() + m.getChargeMoney());
        }
		/*cart.setTotalGoods(item.getCount() + cart.getTotalGoods());
		cart.setTotalProduct(cart.getTotalProduct()+1);*/
        order.setPaidMoney(0);
        User buyer = frontUserService.select(order.getUuid());
        if (buyer != null) {
            order.setExtra(DataName.BUYER.name(), buyer.getNickName() == null ? String.valueOf(buyer.getUuid()) : buyer.getNickName().trim());
            order.setExtra(DataName.avatarUrl.name(), buyer.getAvatar());
        } else {
            order.setExtra(DataName.BUYER.name(), String.valueOf(order.getUuid()));
        }
        logger.debug("添加交易[" + item.getTransactionId() + "]后，购物车[" + order.getId() + "/" + order.getGoodsDesc() + "]商品总数:" + order.getTotalProduct() + ",物品总数:" + order.getTotalGoods() + ",购物车总资金是:" + order.getTotalMoney());
        update(order);
        return order;
    }

    @Override
    public int update(Order cart) {
        Assert.notNull(cart, "尝试更新的Order不能为空");
        if (cart.getTotalGoods() < 0) {
            cart.setTotalGoods(0);
        }
        if (cart.getTotalProduct() < 0) {
            cart.setTotalProduct(0);
        }

        Order _oldOrder = mapper.select(cart.getId());
        if (_oldOrder == null) {
            logger.warn("没有找到对应的cart对象:{}，改为insert", cart.getId());
            return mapper.insert(cart);
        } else {
            return mapper.update(cart);
        }

    }

    @Override
    public int insert(Order order) {
        Assert.notNull(order, "尝试新增的Order不能为空");
        /*if (order.getId() < 1) {
            order.setId(createNewOrderId());
            logger.info("为新增订单新建ID:" + order.getId());
        }*/
        if (select(order.getId()) != null) {
            logger.warn("本地已有相同主键#{}的Order订单存在", order.getId());
            throw new EisException(EisError.BILL_CREATE_FAIL.id, "无法创建订单");
        }
        logger.info("准备新增订单:" + order.getId());
        if (order.getCreateTime() == null) {
            order.setCreateTime(new Date());
        }
        order.setCurrentStatus(TxStatus.waitingPay.id);
        int rs = mapper.insert(order);

        if (rs != 1) {
            return rs;
        }

        if (order.getItemList() != null && order.getItemList().size() > 0) {
            for (Item item : order.getItemList()) {
                item.setCartId(order.getId());
                item.setEnterTime(order.getCreateTime());
                itemService.insert(item);
            }
        }

        return rs;
    }



    @Override
    public HashMap<String, Item> map(CriteriaMap itemCriteria) {

        List<Item> itemList = itemService.list(itemCriteria);
        if (itemList == null || itemList.size() < 1) {
            return null;
        }
        HashMap<String, Item> cart = new HashMap<String, Item>();
        for (Item item : itemList) {
            cart.put(item.getTransactionId(), item);
        }
        return cart;
    }


    public boolean handleSync(){
        return getBoolProperty(HandlerEnum.HANDLE_ORDER.name());
    }

    @Override
    public int count(long uuid, int status) {

        CriteriaMap itemCriteria = new CriteriaMap();
        itemCriteria.put("chargeFromAccount", uuid);
        itemCriteria.putArray("currentStatus", status);//只列出
        return itemService.count(itemCriteria);
    }

    @Override
    public int insertSync(Order entity) {
        if(!HANDLE_ORDER){
            return EisError.NOT_HANDLE_NODE.id;
        }
        int rs = insert(entity);
        if (rs == 1 && isMqEnabled()) {
            entity.setSyncFlag(0);
            putCache(entity);
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "insert", entity);
        }
        return rs;
    }

    @Override
    public Item select(long uuid, String transactionId) {
        logger.debug("尝试为用户[" + uuid + "]获取订单[" + transactionId + "]...");
        CriteriaMap itemCriteria = new CriteriaMap();
        itemCriteria.put("chargeFromAccount", uuid);
        HashMap<String, Item> cart = map(itemCriteria);
        if (cart == null) {
            logger.debug("购物车为空...");
            return null;
        }
        Item item = cart.get(transactionId);
        if (item == null) {
            logger.debug("购物车中没有该订单[" + transactionId + "]...");
            return null;
        }
        logger.debug("返回订单[" + transactionId + "=" + item.toString() + "].");
        return item;
    }

    @Override
    public void delete(long uuid, String transactionId) throws Exception {

        Item item = select(uuid, transactionId);
        if (item == null) {
            logger.warn("没找到尝试删除的订单[" + transactionId + "]");
            return;
        }
        if (item.getChargeFromAccount() != uuid) {
            logger.warn("订单[" + transactionId + "]不属于[" + uuid + "]，属于[" + item.getChargeFromAccount() + "]，不能删除");
            return;
        }
		/*if(cacheItem.getCurrentStatus() != TxStatus.inOrder.getId()){
			logger.warn("缓存中的订单[" + transactionId + "]不是购物车中状态，是[" + cacheItem.getCurrentStatus() + ",不能删除");
			return;
		}*/
        float count = item.getCount();
        if (count <= 0) {
            count = 1;
        }
        long cartId = 0;
        if (item.getId() > 0) {
            cartId = item.getId();
        }

        int rs = itemService.deleteBy(CriteriaMap.create().put("transactionId",transactionId));
        logger.debug("当前交易保存到数据库，尝试删除数据库中的交易:" + transactionId + "，结果:" + rs);

        logger.info("已删除订单[" + transactionId + "]");
        if (cartId > 0) {
            Order cart = select(cartId);
            if (cart == null) {
                logger.debug("找不到删除订单[" + transactionId + "]对应的购物车:" + cartId);
            } else {
                logger.debug("更新订单[" + item.getTransactionId() + "]对应的购物车:" + cartId + "，将其总物品数从" + cart.getTotalGoods() + "-" + count + ",总商品数从" + cart.getTotalProduct() + "-1");
                cart.setTotalGoods(cart.getTotalGoods() - count);
                cart.setTotalProduct(cart.getTotalProduct() - 1);
                update(cart);
            }
        }

    }

    @Override
    public void clear(long uuid) throws Exception {
        //清除购物车
        CriteriaMap itemCriteria = new CriteriaMap();
        itemCriteria.put("chargeFromAccount", uuid);
        List<Item> cart = itemService.list(itemCriteria);
        if (cart == null || cart.size() < 1) {
            logger.info("用户[" + uuid + "]购物车为空，无需清空");
            return;
        }
        for (Item item : cart) {
            int rs = itemService.deleteBy(CriteriaMap.create().put("transactionId",item.getTransactionId()));
        }
        logger.info("用户[" + uuid + "]购物车已清空[" + cart.size() + "]");
    }

    @Override
    public void update(Item item, float changeCount) throws Exception {
		/*Product product = productService.select(item.getProductId());
		if(product == null){
			logger.error("在尝试修改订单[" + item.getTransactionId() + "]时，找不到对应的产品[" + item.getProductId() + "]");
			return;		
		}
		float totalMoney = 0;

		if(totalMoney == 0){
			try{
				totalMoney = product.getBuyMoney() * item.getCount();
				item.setRequestMoney(totalMoney);
				item.setLabelMoney(totalMoney);
			}catch(Exception e){}
			logger.error("在尝试修改订单[" + item.getTransactionId() + "]时，总金额异常为0，修正为" + totalMoney);
		}
		if(totalMoney == 0){			
			logger.error("在尝试修改订单[" + item.getTransactionId() + "]时，总金额异常为0");
			return;	
		}*/
        float count = item.getCount();
        if (count <= 0) {
            count = 1;
        }
        long cartId = 0;
        if (item.getId() > 0) {
            cartId = item.getId();
        }
        if (item.getPrice() == null) {
            throw new EisException(EisError.priceNotExist.id, "更新的订单[" + item.getTransactionId() + "]没有价格对象");
        }
        applyPrice(item, item.getPrice());
        itemService.update(item);

        if (cartId > 0) {
            Order cart = select(cartId);
            if (cart == null) {
                logger.debug("找不到要更新订单[" + item.getTransactionId() + "]对应的购物车:" + cartId);
            } else {
                logger.debug("更新订单[" + item.getTransactionId() + "]对应的购物车:" + cartId + "，将其总物品数从" + cart.getTotalGoods() + "+" + count);
                cart.setTotalGoods(cart.getTotalGoods() + count);
                update(cart);
            }
        }
    }

    private void applyPrice(Item item, Price price) {
        // TODO Auto-generated method stub

    }

    @Override
    public Order getCurrentOrder(long uuid, String priceType, String orderType, String identify, long ownerId, boolean directBuy) {
        if (uuid <= 0) {
            logger.error("尝试获取当前购物车ID的UUID为空");
            return null;
        }
        CriteriaMap cartCriteria = CriteriaMap.create(ownerId);
        cartCriteria.put("priceType", priceType);
        cartCriteria.put("identify", identify);
        cartCriteria.put("uuid", uuid);
        if (orderType == null) {
            orderType = Order.ORDER_TYPE_TEMP;
        }
        cartCriteria.put("orderType", orderType);

        if (directBuy) {
            Order cart = new Order(ownerId);
            cart.setUuid(uuid);
            cart.setIdentify(identify);
            cart.setPriceType(priceType);
            cart.setOrderType(orderType);
            cart.setCurrentStatus(TxStatus.newOrder.id);
            cart.setBuyType(Order.BUY_TYPE_DIRIECT);
            int rs = insert(cart);
            logger.debug("当前请求直接购买，将创建新的购物车，创建结果:" + rs + ",cartId=" + cart.getId());
            if (rs == 1) {
                //XXX 由于此处是内部调用的insert，不会被JMS切面捕获到，因此需要手工发布数据同步
                messageService.sendJmsDataSyncMessage(MessageBusEnum.TX.name(), "cartService", "insert", cart);
                return cart;
            } else {
                logger.error("无法创建新的购物车");
                return null;
            }
        }

        cartCriteria.put("buyType", Order.BUY_TYPE_NORMAL);


        List<Order> cartList = list(cartCriteria);
        if (cartList == null || cartList.size() < 1) {
            logger.debug("找不到符合条件[" + cartCriteria + "]的购物车，创建新的购物车");
            Order cart = new Order(ownerId);
            cart.setUuid(uuid);
            cart.setIdentify(identify);
            cart.setPriceType(priceType);
            cart.setOrderType(orderType);
            cart.setBuyType(Order.BUY_TYPE_NORMAL);
            int rs = insert(cart);
            logger.debug("找不到符合条件[" + cartCriteria + "]的购物车，创建新的购物车，创建结果:" + rs + ",cartId=" + cart.getId());
            if (rs == 1) {
                //XXX 由于此处是内部调用的insert，不会被JMS切面捕获到，因此需要手工发布数据同步
                messageService.sendJmsDataSyncMessage(MessageBusEnum.TX.name(), "cartService", "insert", cart);
                return cart;
            } else {
                logger.error("无法创建新的购物车");
                return null;
            }
        } else {
            //由于修改了Order.xml中的排序为create_time DESC，因此第一个Order就是最后创建的那一个,NetSnake,2016-06-30
            Order cart = cartList.get(0);
            //			Order cart = cartList.get(cartList.size() - 1);
            logger.debug("找到了符合条件[" + cartCriteria + "]的购物车，共" + cartList.size() + "个，返回第一个:" + cart.getId());
            return cart;
        }

    }

    @Override
    public long createNewOrderId() {

        return globalUniqueService.incrSequence(ObjectType.order.name());

		/*long uuid = globalUniqueService.incrOrderSequence(1);
		if(uuid < 1){
			logger.error("无法生成全局唯一ID");
			return -1;
		}
		int cartId = Integer.parseInt(configService.getServerId() + "" + uuid);

		return cartId;*/
    }






    /**
     * 回收一个Order订单
     * 不考虑其中的Item子交易
     * 将状态改为已超时
     * 如果有半支付情况，则尝试退款
     */
    @Override
    public int recycle(Order order) {
        boolean noRefund = false;
        if (order.getCurrentStatus() == TxStatus.timeout.id) {
            logger.warn("订单[" + order.getId() + "]已经是超时状态，不进行退款");
            noRefund = true;
        }
        order.setCurrentStatus(TxStatus.timeout.id);
        CriteriaMap itemCriteria = new CriteriaMap();
        itemCriteria.put("cartId", order.getId());
        if (order.getCreateTime() != null) {
            itemCriteria.put("enterTimeBegin", DateUtils.truncate(order.getCreateTime(), Calendar.DAY_OF_MONTH));
            itemCriteria.put("enterTimeEnd", DateUtils.ceiling(order.getCreateTime(), Calendar.DAY_OF_MONTH));
        }
        List<Item> itemList = itemService.list(itemCriteria);
        logger.debug("订单[" + order.getId() + "]对应的交易品数量是:" + (itemList == null ? "空" : itemList.size()));
        if (itemList != null && itemList.size() > 0) {
            float subOrderMoney = 0;
            for (Item item : itemList) {
                subOrderMoney += item.getRequestMoney();
            }
            order.setTotalMoney(order.getTotalMoney() + subOrderMoney);
        }
        int rs = update(order);

        messageService.sendJmsDataSyncMessage(MessageBusEnum.TX.name(), "cartService", "updateNoNull", order);
        if (rs != 1) {
            logger.error("无法将订单[" + order.getId() + "]回收:" + rs);
            return rs;
        }
        boolean coinPaied = order.getBooleanExtra(DataName.coinPaid.toString());
        if (coinPaied && !noRefund) {
            CriteriaMap payCriteria = CriteriaMap.create(order.getOwnerId());
            payCriteria.put("refBuyTransactionId", String.valueOf(order.getId()));
            payCriteria.put("moneyTypeId", MoneyType.coin.getId());
            payCriteria.putArray("currentStatus", TxStatus.inProcess.id);
            List<Pay> payList = payService.list(payCriteria);
            if (payList == null || payList.size() < 1) {
                logger.warn("找不到订单[" + order.getId() + "]对应的任何已进行coin支付的支付订单");
                return -EisError.REQUIRED_PARAMETER.id;
            }
            for (Pay pay : payList) {
                payService.refund(pay,null);
            }
        } else {
            logger.debug("订单[" + order.getId() + "]未进行coin支付，不需要退款");
        }
        return rs;

    }



    @Override
    public void finish(Order order) {
        order.setCurrentStatus(TxStatus.waitingMinusMoney.id);
        if (mqEnabled) {
            EisMessage eisMessage = new EisMessage();
            eisMessage.setCode(Operate.create.getId());
            eisMessage.setExtra("order", order);
            eisMessage.setObjectType(ObjectType.order.toString());
            messageService.send(MessageBusEnum.TX.name(), eisMessage);
        } else {
            //直接在本地执行交易的后续操作
            TxExecutor exector = transPlanService.getTransactionExecutor(null, 0);
            try {
                exector.begin(order);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    @Transactional
    public int managerApprove(CriteriaMap approveCriteria, Order managerOrder) {

        logger.info("准备审批订单:" + managerOrder.getId() + ",审批用户订单的条件:" + JsonUtils.toStringFull(approveCriteria));
        int rs = 0;
        if (managerOrder != null) {
            rs = mapper.insert(managerOrder);
        } else {
            rs = 1;
        }
        if (rs == 1) {
            //approveCriteria.remove("id");
            int rs2 = mapper.updateBatch(approveCriteria);
            if (rs2 <= 0) {
                throw new RuntimeException("Manager approve failed, update batch return:" + rs2);
            }
        }
        return rs;
    }

    @Override
    public Order sum(CriteriaMap cartCriteria) {
        return mapper.sum(cartCriteria);
    }

    /**
     * 单个订单进行发货，设置快递单号
     *
     * @param order
     * @return
     */
    @Override
    @Transactional
    public int beginDelivery(Order order, String companyCode, String deliveryCode) {
        //不允许待支付或新订单状态发货

        List<DeliveryOrder> deoList = deliveryOrderService.list(new CriteriaMap().put("refOrderId", String.valueOf(order.getId())));

        TxStatus deliveryStatus = TxStatus.delivering;
        if (companyCode != null && companyCode.equalsIgnoreCase("offline")) {
            deliveryStatus = TxStatus.deliveryConfirmed;
        }

        order.setExtraStatus(deliveryStatus.name());

        if (deoList.size() > 0) {
            DeliveryOrder deo = deoList.get(0);
            deo.setOutOrderId(deliveryCode);
            deo.setDeliveryCompany(companyCode);
            deliveryOrderService.update(deo);
            order.setDeliveryOrderId(deo.getId());
        } else {
            DeliveryOrder deo = new DeliveryOrder();
            deo.setCreateTime(new Date());
            deo.setRefOrderId(String.valueOf(order.getId()));
            if (companyCode == null || companyCode.equalsIgnoreCase("offline")) {
            } else {
                deo.setOutOrderId(deliveryCode);
            }
            deo.setDeliveryCompany(companyCode);
            deo.setOwnerId(order.getOwnerId());
            deliveryOrderService.insert(deo);
            order.setDeliveryOrderId(deo.getId());
        }

        int rs = updateSync(order);
        return rs;
    }

    @Override
    public int beginBatchDelivery(Order manageOrder) {
        manageOrder.setLockStatus(manageOrder.getCurrentStatus());
        manageOrder.setCurrentStatus(TxStatus.delivering.id);
        int rs = update(manageOrder);
        if (rs == 1) {
            //批量更新
            CriteriaMap criteriaMap = CriteriaMap.create().
                    put("identify", manageOrder.getId()).
                    put("currentStatus", manageOrder.getCurrentStatus()).
                    put("lockStatus", TxStatus.preDelivery.id).put("mustNullIdentify", false);
            mapper.updateBatch(criteriaMap);
        }
        return 0;
    }



   /* @Override
    public int insertSync(Order order) {
        //后端接收到订单
        boolean handlerOrder = StringTools.isPositive(configService.getProperty(HandlerEnum.HANDLE_ORDER.name()));
        if (handlerOrder) {
            logger.info("本节点处理订单:" + order.getId());
            int rs = insert(order);
            if (rs != 1) {
                logger.error("插入订单失败:" + order.getId());
                return 0;
            }
            logger.info("本节点完成了订单:" + order.getId() + "处理，发送到消息总线");
            order.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(MessageBusEnum.TX.name(), "orderService", "insert", order);
            return 1;
        }

        return 0;
    }*/

    /*@Override
    public int updateSync(Order order) {
        //后端接收到订单
        boolean handlerOrder = configService.getBoolProperty(HandlerEnum.HANDLE_ORDER.name());
        if (handlerOrder) {
            logger.info("本节点处理订单:" + order.getId());
            int rs = update(order);
            if (rs != 1) {
                logger.error("更新订单失败:" + order.getId());
                return 0 ;
            }
            logger.info("本节点完成了订单:" + order.getId() + "处理，发送到消息总线");
            order.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(MessageBusEnum.TX.name(), "orderService", "update", order);
            return 1;
        }

        return 0;
    }*/

    @Override
    public List<Item> listItem(Order order) {
        //找出对应的商品
        Date queryTime = DateUtils.truncate(order.getCreateTime(),Calendar.DAY_OF_MONTH);
        List<Item> itemList = itemService.list(CriteriaMap.create(order.getOwnerId()).put("cartId", order.getId()).put("enterTimeBegin", queryTime));
        IndexableEntity firstObj = null;
        if (itemList.size() > 0) {
            for (Item item : itemList) {
                IndexableEntity obj = stockService.getTargetObject(item.getObjectType(), item.getProductId());
                if (firstObj == null && obj instanceof Node) {
                    order.setDto("pic", ((Node) obj).getPic());
                }
                if(obj instanceof Node) {
                    item.setDto(DataName.pic.name(), ((Node) obj).getPic());
                }
                item.getExt().putAll(obj.getData());
                item.getExt().putAll(item.getData());

            }
        }
        order.setItemList(itemList);
        return itemList;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int assignDriver(Order order, User driver) {

        SyncMode syncMode = SyncMode.find(ContextUtils.getProperty(MbConstants.SYNC_MODE));
        order.setLockStatus(order.getCurrentStatus());
        order.setCurrentStatus(TxStatus.preDelivery.id);
        order.setIdentify(String.valueOf(driver.getUuid()));
        order.setExtra("driverId",driver.getUuid());
        driver.setLockStatus(driver.getCurrentStatus());
        driver.setCurrentStatus(UserStatus.locked.id);
        int rs1 = syncMode == SyncMode.ASYNC ? updateAsync(order) : updateSync(order);

        if (rs1 != OpResult.success.id) {
            return 0;
        }
        try {
            int rs = 0;
            rs  = syncMode == SyncMode.ASYNC ? frontUserService.updateAsync(driver) : frontUserService.updateSync(driver);
            return 1;
        } catch (Exception e) {
            throw new EisException("Assign driver error");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int unAssignDriver(Order order, User driver) {
        SyncMode syncMode = SyncMode.find(ContextUtils.getProperty(MbConstants.SYNC_MODE));

        order.setLockStatus(order.getCurrentStatus());
        order.setCurrentStatus(TxStatus.success.id);

        order.setExtra("driverId",null);
        driver.setLockStatus(driver.getCurrentStatus());
        driver.setCurrentStatus(UserStatus.normal.id);
        int rs1 = syncMode == SyncMode.ASYNC ? updateAsync(order) : updateSync(order);
        if (rs1 != OpResult.success.id) {
            return 0;
        }
        try {
            rs1  = syncMode == SyncMode.ASYNC ? frontUserService.updateAsync(driver) : frontUserService.updateSync(driver);
            return 1;
        } catch (Exception e) {
            throw new EisException("Assign driver error");
        }
    }


}
