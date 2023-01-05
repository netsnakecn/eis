package com.maicard.tx.service.impl;

import com.alibaba.fastjson.JSON;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.*;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.impl.ContextUtils;
import com.maicard.money.constants.TxConstants;
import com.maicard.money.entity.Price;
import com.maicard.money.service.PriceService;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserRelation;
import com.maicard.security.service.UserRelationService;
import com.maicard.site.dao.mapper.DocumentMapper;
import com.maicard.site.dao.mapper.NodeMapper;
import com.maicard.site.entity.Document;
import com.maicard.site.entity.Node;
import com.maicard.site.service.DocumentService;
import com.maicard.site.service.NodeService;
import com.maicard.tx.constants.StockMode;
import com.maicard.tx.constants.StockType;
import com.maicard.tx.dto.ItemDto;
import com.maicard.tx.entity.Item;
import com.maicard.tx.service.StockService;
import com.maicard.tx.vo.FeeVo;
import com.maicard.tx.vo.StockVo;
import com.maicard.utils.NumericUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class StockServiceImpl extends BaseService implements StockService {

    @Resource
    private CenterDataService centerDataService;


    @Autowired
    private NodeMapper nodeMapper;

    @Resource
    private NodeService nodeService;

    @Resource
    private ConfigService configService;

    @Resource
    private DocumentMapper documentMapper;


    @Resource
    private DocumentService documentService;

    @Resource
    private UserRelationService userRelationService;

    @Resource
    private PriceService priceService;

    static StockMode stockMode = null;

    @PostConstruct
    public void init(){
        stockMode  = StockMode.find(configService.getValue(TxConstants.STOCK_MODE,0));

    }

    @Override
    public boolean setStock(StockVo stockVo, StockMode stockMode) {
        return _writeCountToCache(stockVo, stockMode);
    }

    @Override
    public float addStock(String stockKey, String shopId, long objectId, float offset) {
        final StockType stockType = StockType.AVAILABLE;

        StockVo stockVo =  this.getStock(stockKey, shopId, objectId);
        float count = stockVo.available;
        if (count < 0) {
            logger.debug("库存:{}/{}的数值为:{}，不更新");
            return 1;
        }
        float newCount = count + offset;
        if (newCount < 0) {
            newCount = 0;
        }


        //XXX 写入数据库是直接写入最新数值，比如剩余数量1000个，，写入缓存是写入要修改的数值，比如减少5个为-5
        if (newCount % 5 == 0) {
            //写入数据库
            //this._writeToDb(objectType, objectId, newCount, offset);
        }
        return this._writeOffsetToCache(stockKey, shopId, objectId, offset, stockType);

    }

    /**
     * 把新的库存数量写入数据库，同时把已售出数量+offset
     *
     * @author GHOST
     */
    private float _writeToDb(String objectType, long objectId, float amount, float offset) {
        if (objectType.equals(ObjectType.node.name())) {
            //强制从数据库获取
            Node node = nodeMapper.select((int) objectId);
            if (node == null) {
                logger.error("找不到:{}/{}的对象", objectType, objectId);
                return 0;
            }
            node.setExtra(DataName.availableCount.name(), String.valueOf(amount));
			/*long soldCount = node.getLongExtraValue(DataName.soldCount.name());
			//如果是卖出，则offset应当是负数，这里的卖出总数就应该是 --得正
			soldCount -= offset;
			if(soldCount < 0) {
				soldCount = 0;
			}
			node.setExtraValue(DataName.soldCount.name(), String.valueOf(soldCount));*/
            nodeMapper.update(node);
            return amount;

        } else {
            //强制从数据库获取
            Document document = documentMapper.select(objectId);
            if (document == null) {
                logger.error("找不到:{}/{}的对象", objectType, objectId);
                return 0;
            }
            document.setExtra(DataName.availableCount.name(), String.valueOf(amount));

			/*long soldCount = document.getLongExtraValue(DataName.soldCount.name());
			//如果是卖出，则offset应当是负数，这里的卖出总数就应该是 --得正
			soldCount -= offset;
			if(soldCount < 0) {
				soldCount = 0;
			}
			document.setExtraValue(DataName.soldCount.name(), String.valueOf(soldCount));*/
            return amount;

        }

    }


    @Override
    public StockVo getStock(String stockKey, String shopId, long objectId) {

        StockVo stockVo = new StockVo(stockKey);

        int DEFAULT_AVAILABLE_COUNT = configService.getIntValue(TxConstants.DEFAULT_AVAILABLE_COUNT, 0);
        if (DEFAULT_AVAILABLE_COUNT == -1) {
            stockVo.amount(-1, -1);
            return stockVo;
        }
        float distributedCount = _getAmountFromCache(stockKey, shopId, objectId, StockType.AVAILABLE);
//		if(distributedCount == -999){
//			//-999表示没有取到数值
//			return stockVo;
//			//distributedCount =  _getAmountFromDb(objectType, objectId);
//			//this._writeCountToCache(objectType, objectId, distributedCount);
//		}
        stockVo.setAvailable(distributedCount);
        distributedCount = _getAmountFromCache(stockKey, shopId, objectId, StockType.LOCKED);
        stockVo.setLockAmount(distributedCount);

        return stockVo;

    }


    public float _getAmountFromDb(String objectType, long objectId) {
        if (objectType.equals(ObjectType.node.name())) {
            //强制从数据库获取
            Node node = nodeMapper.select((int) objectId);
            if (node == null) {
                logger.error("找不到:{}/{}的对象", objectType, objectId);
                return 0;
            } else {
                float amount = node.getFloatExtra(DataName.availableCount.name());
                logger.debug("从数据库读取:{}/{}对象的剩余数量是:{}", objectType, objectId, amount);
                return amount;
            }
        } else {
            //强制从数据库获取
            Document document = documentService.select( objectId);
            if (document == null) {
                logger.error("找不到:{}/{}的对象", objectType, objectId);
                return 0;
            } else {
                float amount = document.getFloatExtra(DataName.availableCount.name());
                logger.debug("从数据库读取:{}/{}对象的剩余数量是:{}", objectType, objectId, amount);
                return amount;
            }
        }

    }

    private float _writeOffsetToCache(String objectType, String shopId, long objectId, float offset, StockType stockType) {

        String prefix = TxConstants.CACHE_STOCK_PREFIX;
        if (stockType == null) {
            stockType = StockType.AVAILABLE;
        }
        prefix += "#" + stockType.name();

        String rootKey = prefix + "#" + objectType;
        if(StringUtils.isNotBlank(shopId)){
            rootKey += "#" + shopId;
        }
        rootKey += "#" + objectId;

        logger.info("增减对象:" + objectType + "#" + objectId + "的[" + rootKey + "]库存,变更值" + offset);
        Jedis jedis = (Jedis) centerDataService.getResource();

        /////////// 事务性执行并提交 //////////////
        List<Object> execResult = null;
        float remain = 0;
        try {
            jedis.watch(rootKey);
            Transaction t = jedis.multi();
            t.incrByFloat(rootKey, offset);
            execResult = t.exec();
        } finally {
            jedis.close();
        }
        /////////////////////////////////////////
        if (execResult == null) {
            logger.warn("请求获取数据[" + rootKey + "]时数据已改变");
            return 0;
        }
        //如果修改后，数值小于1，则回滚


        String value = null;
        StringBuffer sb = new StringBuffer();
        //logger.debug("排他获取的结果数量是:" + execResult.size());
        if (execResult.size() > 0) {
            try {
                for (Object o : execResult) {
                    sb.append(o.toString()).append(",");
                }
                value = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (value == null) {
            logger.error("无法排他性获取数据[" + rootKey + "]，返回值为空");
            return 0;
        }

        remain = _getAmountFromCache(objectType, shopId, objectId, stockType);
        logger.info("强制设置库存:" + objectType + ",id=" + objectId + "=>" + offset + "，库存类型:" + stockType + ",设置后的值:" + remain + "，返回值:" + value);

        return remain;
    }

    private boolean _writeCountToCache(StockVo stockVo, StockMode stockMode) {

        Assert.isTrue(StringUtils.isNotBlank(stockVo.getObjectType()),"设定库存的objectType不能为空");
        Assert.isTrue(stockVo.getObjectId() > 0,"设定库存的objectId不能为空");
        final String objectType = stockVo.getObjectType();
        final long shopId = NumericUtils.parseLong(stockVo.getShopId());
        long objectId = stockVo.getObjectId();
        float fixAmount = stockVo.getAvailable();

        if(stockMode == StockMode.SHOP || stockMode == StockMode.BOTH) {
            Assert.isTrue(shopId > 0, "库存模式:" + stockMode + "下必须指定shopId");
        }


            float v = BigDecimal.valueOf(fixAmount).setScale(Constants.MONEY_ROUND_LENGTH, Constants.MONEY_ROUND_TYPE).floatValue();
        String prefix = TxConstants.CACHE_STOCK_PREFIX;
        final StockType stockType = StockType.AVAILABLE;
        prefix +=   "#" +  stockType.name();

        if(stockMode == StockMode.GLOBAL || stockMode == StockMode.BOTH){
            String key = prefix + "#" + objectType + "#" + objectId;
            logger.info("写入对象:" + key + "的实际库存:" + fixAmount);
            Jedis jedis = (Jedis) centerDataService.getResource();
            /////////// 事务性执行并提交 //////////////
            List<Object> execResult = null;
            try {
                jedis.watch(key);
                Transaction t = jedis.multi();
                t.setex(key,  Constants.CACHE_MAX_TTL, String.valueOf(v));
                execResult = t.exec();
            } finally {
                jedis.close();
            }
            /////////////////////////////////////////
            if (execResult == null) {
                logger.warn("请求获取数据[" + key + "]时数据已改变");
                return false;
            }
            if(stockMode != StockMode.BOTH) {
                return true;
            }
        }
        if(stockMode == StockMode.SHOP || stockMode == StockMode.BOTH){
            String key = prefix + "#" + objectType + "#" + shopId + "#" + objectId;
            logger.info("写入对象:" + key + "的实际库存:" + fixAmount);
            Jedis jedis = (Jedis) centerDataService.getResource();
            /////////// 事务性执行并提交 //////////////
            List<Object> execResult = null;
            try {
                jedis.watch(key);
                Transaction t = jedis.multi();
                t.setex(key,  Constants.CACHE_MAX_TTL, String.valueOf(v));
                execResult = t.exec();
            } finally {
                jedis.close();
            }
            /////////////////////////////////////////
            if (execResult == null) {
                logger.warn("请求获取数据[" + prefix + "]时数据已改变");
                //回滚全局
                return false;
            }
            return true;
        }
        logger.error("不支持的库存模式:" + stockMode);
        return false;

    }


    public float _getAmountFromCache(String stockKey, String shopId, long objectId, StockType stockType) {
        String prefix = TxConstants.CACHE_STOCK_PREFIX;
        if (stockType == null) {
            stockType = StockType.AVAILABLE;
        }
        prefix += "#" + stockType.name();

        String key = prefix + "#" + stockKey;
        if(stockMode == StockMode.BOTH || stockMode == StockMode.SHOP){
            long shopIdLong = NumericUtils.parseLong(shopId);
            Assert.isTrue(shopIdLong > 0, "库存模式:" + stockMode + "下必须指定shopId");
                key += "#" + shopId ;
        }

        key += "#" + objectId;
        String newNumber = centerDataService.get(key);
        logger.debug("在库存模式:" + stockMode + "下获取中心缓存的产品[" + key + "]数量是:" + newNumber);
        if (StringUtils.isBlank(newNumber)) {
            return 0;
        }
        return NumericUtils.parseFloat(newNumber);

    }

    @Override
    public IndexableEntity getTargetObject(String objectType, long objectId) {
        if (objectType.equalsIgnoreCase(ObjectType.node.name())) {
            Node node = nodeService.select((int) objectId);
            if (node == null) {
                logger.error("找不到{}/{}的对象", objectType, objectId);
                return null;
            }
            return node;
        } else if (objectType.equalsIgnoreCase(ObjectType.document.name())) {

            Document document = documentService.select((int) objectId);
            if (document == null) {
                logger.error("找不到{}/{}的对象", objectType, objectId);
                return null;
            }
            return document;
        } else {
            //使用反射获取对象
            String serviceName = StringUtils.uncapitalize(objectType) + "Service";
            Object service = ContextUtils.getBean(serviceName);
            if (service == null) {
                logger.error("找不到服务:{}", serviceName);
                return null;
            }
            Method method = ClassUtils.getMethodIfAvailable(service.getClass(), "select", long.class);
            if (method == null) {
                logger.error("服务:{}没有select(long)方法", serviceName);
                return null;
            }
            try {
                Object result = method.invoke(service, objectId);
                if (result == null) {
                    logger.error("执行{}的select方法返回的类型为空", serviceName);
                } else {
                    logger.info("执行{}的select方法返回的类型为:{}", serviceName, result.getClass().getName());
                }
                if (result instanceof IndexableEntity) {
                    return (IndexableEntity) result;
                } else {
                    logger.error("执行{}的select方法返回的类型为:{}，不是需要的IndexableEntity", serviceName, result.getClass().getName());
                    return null;

                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    @Override
    public IndexableEntity getTargetObject(String objectType, String objectCode, long ownerId) {

        CriteriaMap criteria = CriteriaMap.create(ownerId);
        if (objectType.equalsIgnoreCase(ObjectType.node.name())) {
            criteria.put("name", objectCode);
            List<Node> list = nodeService.list(criteria);
            if (list.size() > 0) {
                return list.get(0);

            }
            return null;
        } else if (objectType.equalsIgnoreCase(ObjectType.document.name())) {
            criteria.put("title", objectCode);
            List<Document> list = null;
            try {
                list = documentService.list(criteria);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list != null && list.size() > 0) {
                return list.get(0);

            }
            return null;

        } else {
            //使用反射获取对象
            criteria.put("name", objectCode);
            String serviceName = StringUtils.uncapitalize(objectType) + "Service";
            Object service = ContextUtils.getBean(serviceName);
            if (service == null) {
                logger.error("找不到服务:{}", serviceName);
                return null;
            }
            Method method = ClassUtils.getMethodIfAvailable(service.getClass(), "list", CriteriaMap.class);
            if (method == null) {
                logger.error("服务:{}没有list(CriteriaMap)方法", serviceName);
                return null;
            }
            try {
                Object result = method.invoke(service, criteria);
                if (result == null) {
                    logger.error("执行{}的select方法返回的类型为空", serviceName);
                } else {
                    logger.info("执行{}的select方法返回的类型为:{}", serviceName, result.getClass().getName());
                }
                if (result instanceof List) {
                    List<?> list = (List) result;

                    return (IndexableEntity) list.get(0);
                } else {
                    logger.error("执行{}的select方法返回的类型为:{}，不是需要的IndexableEntity", serviceName, result.getClass().getName());
                    return null;

                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    @Override
    public FeeVo computerFee(List<ItemDto> itemList) {
        return null;
    }

    /**
     * 锁定指定产品的库存
     * 即把库存从available放到locked
     */
    @Override
    public StockVo lock(StockVo lockModel) {

        Assert.isTrue(StringUtils.isNotBlank(lockModel.getObjectType()),"锁定库存的objectType不能为空");
        Assert.isTrue(lockModel.getObjectId() > 0,"锁定库存的objectId不能为空");
        Assert.isTrue(lockModel.getLockAmount() > 0,"锁定库存的locked数量不能为空");

        String rootObject = lockModel.getObjectType();
        float lockAmount = BigDecimal.valueOf(lockModel.getLockAmount()).setScale(Constants.MONEY_ROUND_LENGTH, Constants.MONEY_ROUND_TYPE).floatValue();
        lockAmount = Math.abs(lockAmount);


        final long objectId = lockModel.getObjectId();


        StockVo stockVo = new StockVo(lockModel.getObjectType(), lockModel.getObjectId());
        String prefix = TxConstants.CACHE_STOCK_PREFIX;

        String minusPrefix = prefix + "#" + StockType.AVAILABLE.name();
        String plusPrefix = prefix + "#" + StockType.LOCKED.name();

        boolean globalLocked = false;
        boolean shopLocked = false;
        if(stockMode == StockMode.GLOBAL || stockMode == StockMode.BOTH){
            //全局锁定
            String globalMinusKey = minusPrefix + "#" + rootObject + "#" + objectId;
            String globalPlusKey = plusPrefix + "#" + rootObject + "#" + objectId;
            globalLocked =  _lock(stockVo, globalPlusKey, globalMinusKey, lockAmount, StockMode.GLOBAL);
            if(!globalLocked){
                return stockVo;
            }

        }
        if(stockMode == StockMode.SHOP || stockMode == StockMode.BOTH){
            long shopId = NumericUtils.parseLong(lockModel.getShopId());
            Assert.isTrue(shopId > 0, "库存模式:" + stockMode + "下必须提交店铺ID");
            String shopMinusKey = minusPrefix + "#" + rootObject + "#" + shopId + "#" + lockModel.getObjectType() + "#" + objectId;
            String shopPlusKey = plusPrefix + "#" + rootObject + "#" + shopId + "#" + lockModel.getObjectType() + "#" + objectId;
            shopLocked = _lock(stockVo, shopPlusKey, shopMinusKey, lockAmount, StockMode.SHOP);
            if(!shopLocked){
                if(globalLocked){
                    //回滚全局
                    StockVo globalRelease = lockModel.clone();
                    this.release(globalRelease);
                }
            }
        }
        return stockVo;

    }

    private boolean _lock(StockVo resultVo, String globalPlusKey, String globalMinusKey, float lockAmount, StockMode stockMode) {
        Jedis jedis = (Jedis) centerDataService.getResource();

        try {
            String oldV = jedis.get(globalMinusKey);
            if (StringUtils.isBlank(oldV) || NumericUtils.parseFloat(oldV) < lockAmount) {
                logger.error("现有库存:" + globalMinusKey + "是:" + oldV + "，减少:" + lockAmount + "后将小于0，不更新");
                return false;
            }
            /////////// 事务性执行并提交 //////////////
            List<Object> execResult = null;

            jedis.watch(globalMinusKey);
            Transaction t = jedis.multi();
            t.incrByFloat(globalMinusKey, -lockAmount);
            t.incrByFloat(globalPlusKey, lockAmount);

            execResult = t.exec();
            /////////////////////////////////////////
            if (execResult == null) {
                logger.warn("请求获取数据[" + globalMinusKey + "]时数据已改变");
            }
            //如果锁定后发现可用小于0，则回滚
            oldV = jedis.get(globalMinusKey);
            if (StringUtils.isBlank(oldV) || NumericUtils.parseFloat(oldV) < 0) {
                logger.error("现有库存:" + lockAmount + "，回滚:" + lockAmount);
                StockVo releaseVo = resultVo.clone();
                this._release(releaseVo,stockMode);
                return false;
            }
            if( stockMode != StockMode.BOTH){
                resultVo.lockAmount = lockAmount;
                logger.info("当前库存模式:" + stockMode + ",已完成锁定，返回:" + resultVo);
                return true;
            }

        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public int release(StockVo... stockList) {
        for (StockVo stockVo : stockList) {
            _release(stockVo, stockMode);
        }

        return 1;
    }

    private void _release(StockVo releaseModel, StockMode stockMode) {
        Assert.isTrue(StringUtils.isNotBlank(releaseModel.getObjectType()),"锁定库存的objectType不能为空");
        Assert.isTrue(releaseModel.getObjectId() > 0,"锁定库存的objectId不能为空");
        Assert.isTrue(releaseModel.getLockAmount() > 0,"锁定库存的locked数量不能为空");
        String objectType = releaseModel.getObjectType();


        long objectId = releaseModel.objectId;
        float lockAmount = BigDecimal.valueOf(releaseModel.lockAmount).setScale(Constants.MONEY_ROUND_LENGTH, Constants.MONEY_ROUND_TYPE).floatValue();
        lockAmount = Math.abs(lockAmount);

        String prefix = TxConstants.CACHE_STOCK_PREFIX;
        if(stockMode == StockMode.SHOP){
            long shopId = NumericUtils.parseLong(releaseModel.getShopId());
            Assert.isTrue(shopId > 0, "库存模式:" + stockMode + "必须指定shopId");
        }

        String minusPrefix = prefix + "#" + StockType.LOCKED.name();
        String plusPrefix = prefix + "#" + StockType.AVAILABLE.name();

        String minusKey = minusPrefix + "#" + objectType;
        String plusKey = plusPrefix + "#" + objectType;
        if(stockMode == StockMode.SHOP){
            minusKey += "#" + releaseModel.getShopId();
            plusKey += "#" + releaseModel.getShopId();
        }
        minusKey +=  "#" + objectId;
        plusKey +=  "#" + objectId;

        logger.info("解锁库存:" + releaseModel);
        Jedis jedis = (Jedis) centerDataService.getResource();
        /////////// 事务性执行并提交 //////////////
        List<Object> execResult = null;
        try {
            jedis.watch(minusKey);
            Transaction t = jedis.multi();
            t.incrByFloat(minusKey, -lockAmount);
            t.incrByFloat(plusKey, lockAmount);
            execResult = t.exec();
        } finally {
            jedis.close();
        }
        /////////////////////////////////////////
        if (execResult == null) {
            logger.warn("请求获取数据[" + minusKey + "]时数据已改变");
        }

    }


    @Override
    public IndexableEntity writeItemData(Item item, String objectType, long objectId) {

        IndexableEntity targetObject = null;
        if (objectType.equalsIgnoreCase(ObjectType.node.name())) {
            Node node = nodeMapper.select((int) objectId);
            if (node == null) {
                logger.error("找不到{}/{}的对象", objectType, objectId);
                return null;
            }
            item.setName(node.getTitle());
            item.setExtra(DataName.pic.name(),node.getPic());
            item.setObjectTypeId(node.getNodeTypeId());
            targetObject = node;
        } else {
            Document document = documentService.select((int) objectId);
            if (document == null) {
                logger.error("找不到{}/{}的对象", objectType, objectId);
                return null;
            }
            item.setName(document.getTitle());
            item.setExtra(DataName.refImage.toString(), document.getExtra(DataName.thumbnail.name()));
            item.setObjectType(objectType);
            item.setObjectTypeId(document.getId());
            if (document.getData() != null) {
                for (Map.Entry<String,Object> dd : document.getData().entrySet()) {
                    logger.debug("检查目标文档的附加数据:" + JSON.toJSONString(dd));
                    if (dd.getKey().toLowerCase().startsWith("product")) {
                        //必须使用最原生的Map进行赋值
                        //因为要把很多属性带入

                        item.setDto(dd.getKey(), dd.getValue());
                    }
                }
            } else {
                logger.info("目标文档:{}没有任何附加数据", document.getId());
            }
            targetObject = document;
        }

        int ttl = (int) targetObject.getLongExtra(DataName.orderTtl.name());
        if (ttl > 0) {
            item.setTtl(ttl);
        }

        return targetObject;


    }


    @Override
    public int checkPrivilege(Document document, User frontUser) {
        //检查用户是否有权限浏览本文章
        CriteriaMap priceCriteria = CriteriaMap.create(document.getOwnerId());
        priceCriteria.put("objectType", ObjectType.document.name());
        priceCriteria.put("objectId", document.getId());
        priceCriteria.put("priceType", Price.PRICE_STANDARD);
        priceCriteria.putArray("currentStatus", BasicStatus.normal.id);
        Price price = priceService.getPrice(priceCriteria);
        if (price == null || price.isZero()) {
            //没有价格或者价格全部为0，不需要购买也可以看
            if (document.getViewLevel() > 0) {
                if (frontUser == null) {
                    logger.debug("文档:{}阅读级别是:{}，非登录用户无权浏览", document.getId(), document.getViewLevel());
                    return EisError.ACCESS_DENY.id;
                } else if (frontUser.getLevel() <= document.getViewLevel()) {
                    logger.debug("文档:{}阅读级别是:{}，用户:{}级别是:{},无权浏览", document.getId(), document.getViewLevel(), frontUser.getUuid(), frontUser.getLevel());
                    return EisError.REQUIRE_HIGH_LEVEL.id;
                }
            }

        }
        if (frontUser == null) {
            logger.debug("文档:{}的价格是:{},非登录用户无权浏览", document.getId(), price);
            return EisError.ACCESS_DENY.id;
        }
        if (document.getViewLevel() > 0) {
            if (frontUser.getLevel() <= document.getViewLevel()) {
                logger.debug("文档:{}阅读级别是:{}，用户:{}级别是:{},无权浏览", document.getId(), document.getViewLevel(), frontUser.getUuid(), frontUser.getLevel());
                return EisError.REQUIRE_HIGH_LEVEL.id;
            }
        }
        CriteriaMap userRelationCriteria = new CriteriaMap();
        userRelationCriteria.put("uuid", frontUser.getUuid());
        userRelationCriteria.put("relationType", UserRelation.RELATION_TYPE_SUBSCRIBE);
        userRelationCriteria.put("objectType", ObjectType.document.name());
        userRelationCriteria.put("objectId", document.getId());
        int count = userRelationService.count(userRelationCriteria);
        logger.debug("用户:{}对文档:{}的订阅数量是:{}", frontUser.getUuid(), document.getId(), count);

        if (count > 0) {
            return OpResult.success.id;
        }
        //检查用户对整个栏目是否有订阅
        userRelationCriteria.init();
        userRelationCriteria.put("uuid", frontUser.getUuid());
        userRelationCriteria.put("relationType", UserRelation.RELATION_TYPE_SUBSCRIBE);
        userRelationCriteria.put("objectType", ObjectType.node.name());
        userRelationCriteria.put("objectId", document.getDefaultNode().getId());
        count = userRelationService.count(userRelationCriteria);
        logger.debug("用户:{}对栏目:{}的订阅数量是:{}", frontUser.getUuid(), document.getDefaultNode().getId(), count);
        if (count > 0) {
            return OpResult.success.id;
        }
        return EisError.subscribeCountError.id;
    }
}
