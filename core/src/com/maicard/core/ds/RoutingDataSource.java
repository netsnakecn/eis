package com.maicard.core.ds;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:52
 * 动态数据源，配置在spring中详细配置见spring配置文件，DataSourceTransactionManager，sqlSessionFactory和SqlSessionTemplate
 * 使用此数据源配置 ，结合注解DataSourceMapper在service类上注解DataSource的key（对应targetDataSources的key）
 * 和DataSourceSwitcherAdapter进行AOP设置数据源动态切换。
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceSwitcher.getDataSourceType();
    }
}
