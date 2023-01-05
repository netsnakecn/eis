package com.maicard.base;

import com.maicard.core.entity.BaseEntity;

import java.util.List;

public interface GlobalSyncService<T extends BaseEntity> extends IService<T> {
    int insertAsync(T entity);

    int insertSync(T entity);

    int updateAsync(T entity);

    int updateSync(T entity);

    //发送删除广播，不删除任何数据，由关键节点处理
    int deleteAsync(T entity);

    //删除本地，并广播
    int deleteSync(T entity);

    //仅删除本地，不广播
    int deleteLocal(T entity);



}
