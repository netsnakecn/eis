package com.maicard.core.service.abs;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.base.IService;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.BaseEntity;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.StringTools;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.cache.RedisCacheManager;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.maicard.core.constants.Constants.CACHE_NAME;

/**
 * 一个基本的分布式服务实现
 * 不实现本地的方法集合
 */
public abstract class AbsBaseService<T extends BaseEntity, M extends IDao<T>> implements IService<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    protected EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer;

    @Autowired
    protected M mapper;

    @Autowired
    protected RedisCacheManager cacheManager;


    protected long genUuid() {
        int serverId = NumericUtils.parseInt(encryptPropertyPlaceholderConfigurer.getProperty("systemServerId"));
        return Long.parseLong(serverId + "" + new SimpleDateFormat("MMddHHmmss").format(new Date()) + "" + RandomStringUtils.randomNumeric(3));
    }

    protected boolean getBoolProperty(String key) {
        String v = encryptPropertyPlaceholderConfigurer.getProperty(key);
        return StringTools.isPositive(v);
    }


    protected T fromCache(final String cacheType, final String key) {
        final String cacheName = CACHE_NAME + "_" + StringUtils.uncapitalize(cacheType);
        Object o = null;
        if (cacheManager.getCache(cacheName) != null) {
            Cache.ValueWrapper vw = cacheManager.getCache(cacheName).get(key);
            if (vw != null) {
                o = vw.get();
                logger.debug("从缓存:" + cacheName + "读取对象:" + key + "=>" + o);
                if (o != null) {
                    try {
                        return (T) o;
                    } catch (Exception e) {
                        logger.error("无法把缓存:" + o.getClass().getName() + "转换为所需的对象");
                    }
                }

            }
        }

        return null;
    }

    protected void putCache(T entity) {
        if (entity == null) {
            logger.error("不允许放入空对象");
            return;
        }
        if (!entity.isCacheable()) {
            logger.warn("对象:" + entity + "不支持缓存");
            return;
        }
        final String cacheName = CACHE_NAME + "_" + entity.getEntityType();
        final String key = entity.getEntityType() + "#" + entity.getId();
        logger.debug("将对象:" + entity + "放入缓存:" + cacheName + "=>" + key);
        try {
            cacheManager.getCache(cacheName).put(key, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void removeCache(T entity) {
        if(entity == null){
            logger.error("尝试从缓存删除的对象为空");
            return;
        }
        final String cacheName = CACHE_NAME + "_" + StringUtils.uncapitalize(entity.getEntityType());
        if (cacheManager.getCache(cacheName) != null && entity.isCacheable()) {
            final String key = entity.getEntityType() + "#" + entity.getId();
            logger.debug("将对象:" + entity + "从缓存:" + cacheName + "删除:" + key);
            Objects.requireNonNull(cacheManager.getCache(cacheName)).evictIfPresent(key);
        }
    }

    @Override
    public T select(long id) {
        return mapper.select(id);
    }


    @Override
    public int insert(T entity) {


        logger.debug("Try to insert new entity:" + entity.getEntityType() + "#" + entity.getId());
        int rs = 0;
        try {
            rs = mapper.insert(entity);
        } catch (DuplicateKeyException e) {
            logger.error("Can not insert entity:" + entity.getEntityType() + "#" + entity.getId() + ", because duplicate key");
            return EisError.dataDuplicate.id;
        }
        if (rs == 1) {
            putCache(entity);
        }
        return rs;
    }

    @Override
    // @CacheEvict(cacheNames = CACHE_NAME, key = "#entity.getObjectType() + '#' + #entity.id", condition = "#entity.isCacheable()")
    public int update(T entity) {
        logger.debug("Try to update entity:" + entity.getEntityType() + "#" + entity.getId());
        int rs = 0;
        if (mapper.count(CriteriaMap.create().put("id", entity.getId())) <= 0) {
            rs = mapper.insert(entity);
        } else {
            rs = mapper.update(entity);
        }
        if (rs == 1 && entity.isCacheable()) {
            putCache(entity);
        }
        return rs;
    }

    @Override
    public int updateBy(CriteriaMap paramMap) {
        CriteriaMap.fixArray(paramMap);
        return mapper.update(paramMap);
    }

    @Override
    public T select(T model) {
        T entity = null;
        if (model.isCacheable()) {
            entity = fromCache(model.getEntityType(), model.getEntityType() + "#" + model.getId());
            if (entity != null) {
                return entity;
            }
        }
        entity = mapper.select(model.getId());
        if (entity != null && entity.isCacheable()) {
            afterFetch(entity);
            putCache(entity);
        }
        return entity;
    }

    @Override
    public T selectOne(CriteriaMap paramMap) {
        List<T> list = list(paramMap);
        if (list == null || list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<T> list(CriteriaMap paramMap) {
        CriteriaMap.fixArray(paramMap);
        List<T> list = new ArrayList<>();

        if (paramMap.getCacheType() != null) {
            List<Long> idList = mapper.listPk(paramMap);
            if (idList == null) {
                return Collections.emptyList();
            }
            String cacheType = paramMap.getCacheType().trim();
            for (long id : idList) {
                final String key = cacheType + "#" + id;
                T cached = fromCache(cacheType, key);
                if (cached == null) {
                    // logger.debug("未能从缓存读取到对象:" + key);
                    T entity = select(id);
                    if (entity != null) {
                        afterFetch(entity);
                        if (entity.isCacheable()) putCache(entity);
                        list.add(entity);
                    }
                } else {
                    list.add(cached);
                }
            }
        } else {
            list = mapper.list(paramMap);
            if (list != null && list.size() > 0) {
                for (T entity : list) {
                    afterFetch(entity);
                }
            }
        }

        return list;

    }

    public void afterFetch(T entity) {
    }

    @Override
    public int delete(long id) {
        return mapper.delete(id);
    }

    @Override
    public int deleteBy(CriteriaMap paramMap) {
        List<T> list = null;
        if (paramMap.getCacheType() != null) {
            list = list(paramMap);
        }
        int rs = mapper.deleteBy(paramMap);
        if (list != null && rs != list.size()) {
            logger.warn("按条件删除删除数量:" + rs + "与对应对象数量:" + list.size() + "不一致");
        }
        if (list != null) {
            list.forEach(this::removeCache);
        }
        return rs;


    }

    @Override
    public int count(CriteriaMap paramMap) {
        return mapper.count(paramMap);
    }

    @Override
    public List<T> listOnPage(CriteriaMap paramMap) {
        //logger.info("执行listOnPage");
        //  CriteriaMap.toOnPage(paramMap);
        //CriteriaMap.fixArray(paramMap);
        return list(paramMap);
    }


}
