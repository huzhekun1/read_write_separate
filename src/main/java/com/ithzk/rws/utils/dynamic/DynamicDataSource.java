package com.ithzk.rws.utils.dynamic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 通过AbstractRoutingDataSource实现动态切换数据源，需重写determineCurrentLookupKey方法
 * 由于DynamicDataSource是单例的，线程不安全的，所以采用ThreadLocal保证线程安全，由DynamicDataSourceHolder完成。
 * @author hzk
 * @date 2019/3/26
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 在spring容器中查询对应key来应用为数据源
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceHolder.getRouteKey();
    }
}
