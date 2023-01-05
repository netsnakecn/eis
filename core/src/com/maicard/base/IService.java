package com.maicard.base;

import java.util.List;
import java.util.Map;

public interface IService<T>   {
    public int insert(T entity);

    public int update(T entity);

    int updateBy(CriteriaMap  paramMap);

    public T select(long id);

    T select(T model);

    public T selectOne(CriteriaMap paramMap);

    public List<T> list(CriteriaMap paramMap);

    public int delete(long id);

    public int deleteBy(CriteriaMap paramMap);

    public int  count(CriteriaMap paramMap);

    public List<T>  listOnPage(CriteriaMap paramMap);
}
