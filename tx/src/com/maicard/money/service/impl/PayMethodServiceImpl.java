package com.maicard.money.service.impl;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.Constants;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.money.dao.mapper.PayMethodMapper;
import com.maicard.money.entity.PayMethod;
import com.maicard.money.service.PayMethodService;
import com.maicard.utils.CacheUtils;
import com.maicard.utils.ClassUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils;

@Service
public class PayMethodServiceImpl extends AbsGlobalSyncService<PayMethod, PayMethodMapper> implements PayMethodService {


    @Resource
    private CenterDataService centerDataService;

    @Resource
    private ConfigService configService;


    String CACHE_TABLE = "PAY_METHOD_CACHE";


    protected static String systemCode = null;


    protected String getCacheKey(PayMethod payMethod) {
        String key = "PayMethod#" + payMethod.getOwnerId() + "#" + systemCode + "#" + payMethod.getId();
        return key;
    }


    @PostConstruct
    public void init() {

        systemCode = configService.getSystemCode();
        CACHE_TABLE += "_" + systemCode;
        logger.info("将表名:{}注册为缓存:{}的一员", CACHE_TABLE, CacheNames.cacheNameProduct);
        CacheUtils.registerRelationCache(CacheNames.cacheNameProduct, CACHE_TABLE);
    }


    @Override
    public int insert(PayMethod payMethod) {
        int rs = mapper.insert(payMethod);
        if (payMethod.getId() > 0) {
            String key = getCacheKey(payMethod);
            try {
                centerDataService.setHmPlainValue(CACHE_TABLE, key, JsonUtils.toStringFull(payMethod), (int) Constants.CACHE_MAX_TTL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rs;
    }

    @Override
    public int update(PayMethod payMethod) {
        int actualRowsAffected = 0;


        actualRowsAffected = super.update(payMethod);
        boolean syncCache = actualRowsAffected > 0 && payMethod.getSyncFlag() == 0;
        logger.info("更新支付通道:{},version={},结果:{}，是否需要刷新缓存:{}", payMethod.getId(), payMethod.getVersion(), actualRowsAffected, syncCache);
        if (syncCache) {
            //由于使用redis cache，因此只有第一次更新才需要更新缓存
            String key = getCacheKey(payMethod);

            try {
                centerDataService.setHmPlainValue(CACHE_TABLE, key, JsonUtils.toStringFull(payMethod), (int) Constants.CACHE_MAX_TTL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return actualRowsAffected;
    }

    @Override
    public int delete(long payMethodId) {
        int actualRowsAffected = 0;

        PayMethod _oldPayMethod = mapper.select(payMethodId);

        if (_oldPayMethod != null) {
            actualRowsAffected = mapper.delete(payMethodId);
        }
        try {
            String key = getCacheKey(_oldPayMethod);
            centerDataService.setHmPlainValue(CACHE_TABLE, key, null, (int) Constants.CACHE_MAX_TTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actualRowsAffected;
    }

    @Override
    public PayMethod select(long payMethodId) {
        return mapper.select(payMethodId);
    }

    private PayMethod select(long payMethodId, long ownerId) {
        Assert.isTrue(payMethodId > 0, "查找的payMethod不能为0");
        String text = null;
        String key = "PayMethod#" + ownerId + "#" + systemCode + "#" + payMethodId;
        try {
            text = centerDataService.getHmPlainValue(CACHE_TABLE, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (text == null) {
            logger.error("未能从缓存:{}中获取到支付通道:{}", CACHE_TABLE, key);
            return mapper.select(payMethodId);
        }
        PayMethod payMethod = null;
        try {
            payMethod = JsonUtils.getInstance().readValue(text, PayMethod.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (payMethod == null) {
            logger.error("未能将缓存数据:{}转换为支付通道:{}", text, payMethodId);
            return mapper.select(payMethodId);
        } else {
            return payMethod;
        }


    }


    @Override
    public List<PayMethod> list(CriteriaMap payMethodCriteria) {

        long ownerId = payMethodCriteria.getLongValue("ownerId");
        Assert.isTrue(ownerId > 0, "列出支付通道的ownerId不能为0");
        Set<String> keys = centerDataService.getHmKeys(CACHE_TABLE);
        if (keys == null || keys.size() < 1) {
            initCache(ownerId);
            keys = centerDataService.getHmKeys(CACHE_TABLE);
        }
        List<PayMethod> list = new ArrayList<PayMethod>();
        //从缓存中获取所有支付方式
        for (Object pk : keys) {

            String pkS = pk.toString();
            if (!pkS.startsWith("PayMethod")) {
                continue;
            }

            String[] tempData = pkS.split("#");
            //long currentOwnerId = NumericUtils.parseLong(tempData[1]);
            String currentCode = tempData[2];
            //if(currentOwnerId != ownerId) {
            //	logger.debug("忽略ownerId={}与当前ownerId={}不一致的通道:{}", currentOwnerId, ownerId, pkS);
            //	continue;
            //}
            if (!currentCode.equals(systemCode)) {
                logger.debug("忽略systemCode={}与当前systemCode={}不一致的通道:{}", currentCode, systemCode, pkS);
                continue;
            }
            String pkStr = tempData[3];
            logger.debug("准备从缓存查找支付通道:{}={}", pkS, pkStr);
            long payMethodId = NumericUtils.parseLong(pkStr);
            PayMethod payMethod = select(payMethodId, ownerId);
            if (payMethod != null) {
                list.add(payMethod);
            } else {
                logger.error("未能获取支付通道:{}", payMethodId);
            }
        }
        if (list.size() < 1) {
            logger.error("从系统中获取到的支付通道数量是0");
            return Collections.emptyList();
        }
        List<PayMethod> filteredList = null;
        try {
            filteredList = ClassUtils.search(list, payMethodCriteria);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | IntrospectionException e) {
            e.printStackTrace();
        }
        if (filteredList == null) {
            return Collections.emptyList();
        }
        return filteredList;
		/*
		List<Integer> pkList = payMethodDao.listPkOnPage(payMethodCriteria);
		if(pkList == null || pkList.size() < 1) {
			return Collections.emptyList();
		}
		for(Integer pk : pkList) {
			PayMethod payMethod = payMethodDao.select(pk);
			if(payMethod != null) {
				list.add(payMethod);
			}
		}
		return list;*/

    }

    private void initCache(long ownerId) {
        CriteriaMap critera = CriteriaMap.create(ownerId);
        List<Long> pkList = mapper.listPk(critera);
        if (pkList == null || pkList.size() < 1) {
            logger.warn("系统中没有任何ownerId={}的支付通道", ownerId);
            return;
        }
        try {
            for (long pk : pkList) {
                PayMethod payMethod = mapper.select(pk);
                if (payMethod == null) {
                    logger.error("从系统中找不到支付通道:{}", pk);
                    continue;
                }
                String key = getCacheKey(payMethod);


                centerDataService.setHmPlainValue(CACHE_TABLE, key, JsonUtils.toStringFull(payMethod), (int) Constants.CACHE_MAX_TTL);
            }
            logger.info("初始化{}个支付通道到缓存", pkList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PayMethod> listOnPage(CriteriaMap payMethodCriteria) {
        List<PayMethod> list = new ArrayList<PayMethod>();
        List<Long> pkList = mapper.listPk(payMethodCriteria);
        if (pkList == null || pkList.size() < 1) {
            return Collections.emptyList();
        }
        for (long pk : pkList) {
            PayMethod payMethod = mapper.select(pk);
            if (payMethod != null) {
                list.add(payMethod);
            } else {
                logger.error("找不到指定的支付方式:{}", pk);
            }
        }
        return list;

    }

    /**
     * 获取payMethod id 为key 的 map
     *
     * @param payMethodCriteria
     * @return
     * @throws Exception
     */
    @Override
    public Map<Long, PayMethod> list4IdKeyMap(CriteriaMap payMethodCriteria) {
        Map<Long, PayMethod> payMethodMap = new HashMap<Long, PayMethod>();
        List<PayMethod> payMethodList = list(payMethodCriteria);
        if (payMethodList == null) {
            return payMethodMap;
        }
        for (PayMethod payMethod : payMethodList) {
            payMethodMap.put(payMethod.getId(), payMethod);
        }
        return payMethodMap;
    }

    @Override
    public int count(CriteriaMap payMethodCriteria) {
        return mapper.count(payMethodCriteria);
    }

    @Override
    public void combineExtraData(PayMethod payMethod, String displayLevel, int columnSize) {


        Map<String, Object> dataMap = payMethod.getData();
        payMethod.initExtra();
        if (dataMap == null) {
            dataMap = payMethod.getData();
        }

        //TODO 对不符合显示条件的可以进行滤除

        if (columnSize < 1) {
            return;
        }
        int paddingCnt = dataMap.size() % columnSize;
        if (paddingCnt > 0) {
            paddingCnt = 3 - paddingCnt;
            logger.info("扩展数据数量是:{}，需要填充{}个空数据", dataMap.size(), paddingCnt);
            for (int i = 0; i < paddingCnt; i++) {
                //补几个空对象
                String paddingName = "_padding_" + i;
                dataMap.put(paddingName, "");
            }
        }

        payMethod.setData(dataMap);
    }
}
