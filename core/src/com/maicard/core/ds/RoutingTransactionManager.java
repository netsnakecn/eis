package com.maicard.core.ds;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:58
 * To change this template use File | Settings | File Templates.
 */
public class RoutingTransactionManager implements PlatformTransactionManager {

    private Map<String, PlatformTransactionManager> targetTransactionManagers;
    private final String DEFAULT_TRANSACTIONMANAGER = "DEFAULT_TRANSACTIONMANAGER";

    public RoutingTransactionManager() {
        this.targetTransactionManagers = new ConcurrentHashMap<String, PlatformTransactionManager>();
    }

    protected PlatformTransactionManager getTargetTransactionManager() {
        String type = DataSourceSwitcher.getDataSourceType();
        return targetTransactionManagers.get(StringUtils.isEmpty(type) ? DEFAULT_TRANSACTIONMANAGER : type);
    }

    public void setDefaultTransactionManager(PlatformTransactionManager transactionManager) {
        this.targetTransactionManagers.put(DEFAULT_TRANSACTIONMANAGER, transactionManager);
    }

    /**
     * <ul>
     * <li>4、方法含义：</li>
     * <li>5、方法说明：</li>
     * </ul>
     *
     * @see org.springframework.transaction.PlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
     */
    @Override
    public TransactionStatus getTransaction(TransactionDefinition paramTransactionDefinition) throws TransactionException {
        return getTargetTransactionManager().getTransaction(paramTransactionDefinition);
    }

    /**
     * <ul>
     * <li>1、开发日期：2014-8-6</li>
     * <li>2、开发时间：上午9:54:19</li>
     * <li>3、作          者：wangliang</li>
     * <li>4、方法含义：</li>
     * <li>5、方法说明：</li>
     * </ul>
     *
     * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
     */
    @Override
    public void commit(TransactionStatus paramTransactionStatus) throws TransactionException {
        getTargetTransactionManager().commit(paramTransactionStatus);
    }

    /**
     * <ul>
     * <li>1、开发日期：2014-8-6</li>
     * <li>2、开发时间：上午9:54:19</li>
     * <li>3、作          者：wangliang</li>
     * <li>4、方法含义：</li>
     * <li>5、方法说明：</li>
     * </ul>
     *
     * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
     */
    @Override
    public void rollback(TransactionStatus paramTransactionStatus) throws TransactionException {
        getTargetTransactionManager().rollback(paramTransactionStatus);
    }
}
