package com.maicard.core.service.abs;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IDao;
import com.maicard.base.ImplNameTranslate;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.HandlerEnum;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.mb.constants.MessageBusEnum;
import com.maicard.mb.service.MessageService;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.StringTools;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.maicard.core.constants.Constants.CACHE_NAME;

/**
 * 一个基本的分布式服务实现
 * 不实现本地的方法集合
 */
public abstract class AbsGlobalSyncService<T extends BaseEntity, M extends IDao<T>> extends AbsBaseService<T,M> implements GlobalSyncService<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    protected MessageService messageService;



    protected MessageBusEnum messageBusEnum = MessageBusEnum.NORMAL;


    protected boolean isMqEnabled() {
        return getBoolProperty(DataName.MQ_ENABLED.name());
    }

    public boolean handleSync(){
        return getBoolProperty(HandlerEnum.HANDLE_SYNC.name());
    }

    @Override
    public int insertAsync(T entity) {
        if (!isMqEnabled()) {
            return insert(entity);
        }
        messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "insertSync", entity);
        return 1;
    }


    @Override
    public int insertSync(T entity) {
        if(!handleSync() && isMqEnabled()){
            return EisError.NOT_HANDLE_NODE.id;
        }
        int rs = insert(entity);
        if(rs == 1 && entity.isCacheable()){
            putCache(entity);
        }
        if (rs == 1 && isMqEnabled()) {
            entity.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "insert", entity);
        }
        return rs;
    }


    @Override
    public int updateAsync(T entity) {
        removeCache(entity);
        if (!isMqEnabled()) {
            return update(entity);
        }
        messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "updateSync", entity);
        return 1;
    }

    @Override
    public int updateSync(T entity) {
        if(!handleSync() && isMqEnabled()){
            return EisError.NOT_HANDLE_NODE.id;
        }
        int rs = update(entity);
        if(rs == 1 && entity.isCacheable()){
            putCache(entity);
        }
        if (rs == 1 && isMqEnabled()) {
            entity.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "update", entity);
        }
        return rs;
    }

    @Override
    public int deleteAsync(T entity) {
        Assert.isTrue(entity.getId() > 0, "Remove object id is zero");
        if (!isMqEnabled()) {
            return delete(entity.getId());
        }

        String key = entity.getObject() + "#" + entity.getId();
        //removeCache(entity);
        entity.setSyncFlag(0);
        messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "deleteSync", entity);
        return 1;
    }

    @Override
    public int deleteSync(T entity) {
        if(!handleSync() && isMqEnabled()){
            return EisError.NOT_HANDLE_NODE.id;
        }
        entity.setSyncFlag(0);
        int rs = deleteLocal(entity);
        if (rs == 1 && isMqEnabled()) {
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "deleteLocal", entity);
        }
        return rs;
    }

    @Override
    public int deleteLocal(T entity){
        if(entity.isCacheable()){
            removeCache(entity);
        }
        return delete(entity.getId());
    }
}


