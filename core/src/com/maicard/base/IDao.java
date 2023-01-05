package com.maicard.base;

import java.util.List;
import java.util.Map;


public interface IDao<T> {

    public int insert(T entity);

    public int update(T entity);

    int update(CriteriaMap  params);

    public T select(long id);

    public List<T> list(Map<String,Object> params);

    public int delete(long id);

    public int deleteBy(CriteriaMap params);

    public int  count(CriteriaMap params);

    List<Long> listPk(CriteriaMap params);
}
