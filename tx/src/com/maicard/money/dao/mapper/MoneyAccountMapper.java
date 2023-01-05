package com.maicard.money.dao.mapper;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.money.entity.MoneyAccount;

import java.util.List;
import java.util.Map;

 
public interface MoneyAccountMapper extends IDao<MoneyAccount> {

    /**
     * 根据账户号获取账户信息
     * @param accountNo
     * @return
     */
    public MoneyAccount queryByAccountNo(String accountNo);

    /**
     * 根据 获取账户列表
     * @param v
     * @return
     */
    public List<MoneyAccount> queryAccountList(Map<String,Object> v);

    /**
     * 修改账户状态
     * @param map
     * @return
     */
    public int updateStatus(Map<String, Object> map);



    /**
     * 账户资金进行调增
     * @param map
     * @return
     */
    public int increase(Map<String, Object> map);


    /**
     * 账户资金进行扣减和增加相对 使用乐观锁
     * @param map
     * @return
     */
    public int decrease4OptLock(Map<String, Object> map);
    
    //直接扣减，不处理有效资金和扣减之间的关系
    public int minus4OptLock(Map<String,Object> map);

    /**
     * 查询账户基本信息
     * @param map
     * @return
     */
    public List<MoneyAccount> getBasicByMap(Map<String, Object> map);

    
    
    public List<MoneyAccount> getTotal(CriteriaMap criteria);
    
}
