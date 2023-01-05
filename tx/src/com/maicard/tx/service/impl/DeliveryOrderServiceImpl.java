package com.maicard.tx.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.constants.OpResult;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.entity.Price;
import com.maicard.money.vo.DeliveryTraceVo;
import com.maicard.security.service.FrontUserService;
import com.maicard.tx.dao.mapper.DeliveryOrderMapper;
import com.maicard.tx.entity.*;
import com.maicard.tx.iface.DeliveryQueryProcessor;
import com.maicard.tx.service.*;
import com.maicard.tx.utils.AreaTrimUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DeliveryOrderServiceImpl extends AbsGlobalSyncService<DeliveryOrder,DeliveryOrderMapper> implements DeliveryOrderService {


    @Resource
    private ApplicationContextService applicationContextService;

    @Resource
    private ConfigService configService;

    @Resource
    private DeliveryCompanyService deliveryCompanyService;


    @Resource
    private DeliveryPriceService deliveryPriceService;

    @Resource
    private FeeAdjustService feeAdjustService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private FrontUserService frontUserService;

    @Resource
    private StockService stockService;

    static private DeliveryQueryProcessor queryProcessor = null;


    final String globalDefaultFromArea = "北京#北京";

    @Resource
    private GlobalUniqueService globalUniqueService;

    static Map<String, String> DELIVERY_COMPANY_MAP = null;




    @Override
    public DeliveryOrder generateDeliveryOrder(long addressBookId, Item[] items, String refOrderId, String transactionType, String identify) throws Exception {
        Assert.isTrue(addressBookId > 0, "尝试计算邮费必须有配送地址");
        Assert.isTrue(items != null && items.length > 0, "尝试计算邮费至少要有一个订单");

        AddressBook addressBook = addressBookService.select(addressBookId);

        Assert.notNull(addressBook, "找不到计算邮费的地址:" + addressBookId);
        Assert.notNull(addressBook.getProvince(), "计算邮费的地址[" + addressBookId + "]没有省份信息");

        DeliveryOrder totalDeliveryOrder = null;
        //计算每个商品的邮费
        for (Item item : items) {
            DeliveryOrder subOrder = generateDeliveryOrder(item, addressBook);
            if (subOrder != null && subOrder.getFee() != null) {
                if (totalDeliveryOrder == null) {
                    totalDeliveryOrder = subOrder.clone();
                    totalDeliveryOrder.setFee(new Price());
                    totalDeliveryOrder.setId(0);
                }
                totalDeliveryOrder.setFee(Price.add(totalDeliveryOrder.getFee(), subOrder.getFee()));
            }
        }
        if (totalDeliveryOrder == null) {
            logger.warn("为订单[" + refOrderId + "]计算的总配送单是空");
        } else {
            logger.info("为订单[" + refOrderId + "]计算的总配送单费用是:" + totalDeliveryOrder.getFee());

        }
        return totalDeliveryOrder;
		
		
		

		/*ActivityCriteria activityCriteria = new ActivityCriteria();
		activityCriteria.setActivityType(ActivityCriteria.ACTIVITY_TYPE_BUY);
		List<Activity> activityList = activityService.list(activityCriteria);
		if(activityList == null || activityList.size() < 1){
			logger.debug("当前没有任何类型为[" + ActivityCriteria.ACTIVITY_TYPE_BUY + "]的活动");
		} else {
			for(Activity activity : activityList){
				if(activity.getProcessor() == null){
					logger.warn("购买活动[" + activity.getActivityId() + "]没有定义活动处理器");
					continue;
				}
				ActivityProcessor p = (ActivityProcessor)applicationContextService.getBean(activity.getProcessor());
				p.execute(null, activity, deliveryOrder, feeMap);
			}
		}

		Price finalPrice = new Price();

		for(Price p : feeMap.values()){
			Price.add(finalPrice, p);			
		}
		logger.debug("为配送单[" + deliveryOrder + "]生成的最终价格是:" + finalPrice);
		deliveryOrder.setFee(finalPrice);

		return deliveryOrder;*/
    }


    /**
     * 根据交易，得到它的发货地区
     *
     * @param item
     * @return
     */
    @Override
    public String getFromArea(Item item) {
        if (item.getObjectType().equalsIgnoreCase(ObjectType.node.name())) {
            return null;
        }
        if (item.getObjectType().equalsIgnoreCase(ObjectType.document.name())) {
            return null;
        }
        return null;//getFromArea(product);
    }

    @Override
    public String getFromArea(BaseEntity object) {
        Assert.notNull(object, "尝试查找发货地的对象不能为空");

        String defaultFromArea = object.getExtra(DataName.defaultFromArea.toString());
        if (defaultFromArea != null) {
            logger.info("找到了对象[" + object.getEntityType() + "/" + object.getId() + "]的默认发货地:" + defaultFromArea);
            return defaultFromArea;
        }

        logger.info("找不到对象[" + object.getEntityType() + "/" + object.getId() + "]的默认发货地，尝试查找系统配置");
        defaultFromArea = configService.getValue(DataName.defaultFromArea.toString(), object.getOwnerId());
        if (defaultFromArea == null) {
            defaultFromArea = globalDefaultFromArea;
            logger.info("系统未配置默认发货地，使用全局默认配置:" + defaultFromArea);
        }
        return defaultFromArea;
    }

    @Override
    public DeliveryOrder generateDeliveryOrder(Item item, AddressBook addressBook) {
        IndexableEntity product = stockService.getTargetObject(item.getObjectType(), item.getProductId());
        if (product == null) {
            logger.error("找不到订单对应的产品:" + item.getProductId());
            return null;
        }
		
		/*Price itemPrice = items[0].getPrice();
		if(itemPrice == null){
			logger.warn("交易[" + items[0].getTransactionId() + "]没有价格实例");
		}*/

        //获取发货地
        String fromProvince = null;
        String fromArea = null;

        //FIXME
        String from = "";// getFromArea(product);
        String[] data = from.split("#");
        if (data.length != 2) {
            logger.error("无法解析发货地配置:" + from);
            return null;
        }
        fromProvince = data[0];
        fromArea = data[1];


        DeliveryOrder deliveryOrder = new DeliveryOrder(addressBook);
        deliveryOrder.setId(addressBook.getId());
        deliveryOrder.setRefOrderId(item.getTransactionId());
        //deliveryOrder.setObjectType(transactionType);
        //deliveryOrder.setRefOrderId(refOrderId);
        int weight = (int) item.getLongExtra(DataName.goodsWeight.toString());
        if (weight <= 0) {
            logger.warn("订单[" + item.getTransactionId() + "]没有设置商品重量");
        } else {
            float count = item.getCount();
            if (count < 1) {
                count = 1;
            }
            deliveryOrder.setGoodsWeight(weight * count);
            logger.debug("订单[" + item.getTransactionId() + "]的商品重量是:" + weight + ",乘以数量" + count + "后的总重量是:" + deliveryOrder.getGoodsWeight());
        }

        deliveryOrder.setIdentify("product#" + item.getProductId());
        deliveryOrder.setFromProvince(fromProvince);
        deliveryOrder.setFromArea(fromArea);

        if (addressBook.getProvince() == null) {
            logger.error("地址[" + addressBook + "]没有收货地省份信息，无法生成配送单");
            return null;
        }
        //处理省份
        deliveryOrder.setToProvince(AreaTrimUtils.trimArea(addressBook.getProvince()));


        if (addressBook.getCity() != null) {
            deliveryOrder.setToArea(AreaTrimUtils.trimArea(addressBook.getCity()));
        } else if (addressBook.getProvince() != null) {
            deliveryOrder.setToArea("*");
        }

        //得到最优快递公司
        long deliveryPartnerId = product.getLongExtra(DataName.deliveryCompanyId.toString());
        if (deliveryPartnerId > 0) {
            deliveryOrder.setDeliveryCompanyId(deliveryPartnerId);
        } else {
            deliveryOrder.setDeliveryCompanyId(deliveryCompanyService.getBestDeliveryCompanyId(deliveryOrder));
        }

        //计算标准价格
        Price standardPrice = deliveryPriceService.calculatePrice(deliveryOrder);
        logger.debug("为订单[" + deliveryOrder + "]计算标准价格是:" + standardPrice);


        //根据价格Price查找是否有对应的价格减免


        CriteriaMap feeAdjustCriteria = CriteriaMap.create(deliveryOrder.getOwnerId());
        feeAdjustCriteria.put("fromProvince", deliveryOrder.getFromProvince());
        feeAdjustCriteria.put("fromArea", deliveryOrder.getFromArea());
        feeAdjustCriteria.put("toProvince", deliveryOrder.getToProvince());
        feeAdjustCriteria.put("toArea", deliveryOrder.getToArea());
        //feeAdjustCriteria.applyPriceAttributes(itemPrice);
        feeAdjustCriteria.put("deliveryCompanyId", deliveryOrder.getDeliveryCompanyId());
        feeAdjustCriteria.put("beginTime", new Date());
        feeAdjustCriteria.put("endTime", new Date());
        feeAdjustCriteria.putArray("currentStatus", BasicStatus.normal.getId());
        List<FeeAdjust> feeAdjustList = feeAdjustService.list(feeAdjustCriteria);
        boolean clearDeliveryFee = false;
		/*if(feeAdjustList == null || feeAdjustList.size() < 1){
			logger.debug("当前没有针对订单[" + deliveryOrder + "]的价格调整配置");
		} else {
			for(FeeAdjust feeAdjust : feeAdjustList){
				if(feeAdjust.getFee() == null){
					logger.error("价格调整配置[" + feeAdjust.getFeeAdjustId() + "]的调整价格是空");
					continue;
				}
				if(feeAdjust.isClearDeliveryFee()){
					logger.info("当前价格调整[" + feeAdjust.getFeeAdjustId() + "]要求清空快递费");
					clearDeliveryFee = true;
					break;
					
				}
				finalPrice = Price.add(finalPrice, feeAdjust.getFee());		
				logger.debug("为配送单[" + deliveryOrder + "]应用价格调整配置[" + feeAdjust.getFeeAdjustId() + "]的调整价格:" + feeAdjust.getFee());
			}
		}
		if(clearDeliveryFee){
			finalPrice = new Price();
		}
		logger.debug("为配送单[" + deliveryOrder + "]生成的最终价格是:" + finalPrice);*/
        deliveryOrder.setFee(standardPrice);
        return deliveryOrder;
    }

    @Override
    public DeliveryOrder trace(Order order) {
        if (order == null || order.getDeliveryOrderId() <= 0) {
            return null;
        }

        if (queryProcessor == null) {
            queryProcessor = applicationContextService.getBeanGeneric(DeliveryQueryProcessor.class);
        }

        if (queryProcessor == null) {
            logger.info("系统没有类型是:" + DeliveryQueryProcessor.class + "的BEAN");
            return null;
        }
        DeliveryOrder deo = select(order.getDeliveryOrderId());
        if (deo == null) {
            deo = new DeliveryOrder();
        }
        if (deo.getDeliveryCompany() != null) {

            if (deo.getDeliveryCompany().equalsIgnoreCase("offline")) {
                List<DeliveryTraceVo> traceMap = new ArrayList<>();
                DeliveryTraceVo vo = new DeliveryTraceVo();
                deo.setDeliveryCompany("线下");
                vo.setIndex(1);
                vo.setTime(deo.getCreateTime() == null ? (order.getEndTime() == null ? order.getCreateTime() : order.getEndTime()) : deo.getCreateTime());
                vo.setDesc("线下发货，交易已完成");
                traceMap.add(vo);
                deo.setTraceData(traceMap);
                return deo;
            }
            Map<String, String> company = getCompany();
            String companyName = "";
            for (Map.Entry<String, String> entry : company.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(deo.getDeliveryCompany())) {
                    companyName = entry.getValue().trim();
                    break;
                }
            }
            if (StringUtils.isNotBlank(companyName)) {
                deo.setDeliveryCompany(companyName);

            }
        }

        EisMessage msg = queryProcessor.query(deo.getOutOrderId(), deo.getDeliveryCompany(), null);
        if (msg.code == OpResult.success.id) {
            deo.setTraceData(msg.getExtra("traceMap"));
        }
        return deo;
    }

    @Override
    public Map<String, String> getCompany() {

        if (DELIVERY_COMPANY_MAP == null) {
            final String configName = "classpath:pconfig/delivery_company.json";
            String text = StringTools.readClassPathContent(configName);
            try {
                logger.info("读取配置文件:" + configName + "=>" + text);
                DELIVERY_COMPANY_MAP = JsonUtils.getInstance().readValue(text, new TypeReference<Map<String, String>>() {
                });

            } catch (Exception e) {
                logger.error("无法读取或处理快递公司配置文件:" + configName + "=>" + text);
                e.printStackTrace();
            }
        }
        if (DELIVERY_COMPANY_MAP == null) {
            return Collections.emptyMap();
        }
        return DELIVERY_COMPANY_MAP;
    }

}
