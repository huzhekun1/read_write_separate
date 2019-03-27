package com.ithzk.rws.utils.dynamic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过AbstractRoutingDataSource实现动态切换数据源，需重写determineCurrentLookupKey方法
 * 由于DynamicDataSource是单例的，线程不安全的，所以采用ThreadLocal保证线程安全，由DynamicDataSourceHolder完成。
 * @author hzk
 * @date 2019/3/26
 */
public class DynamicMulSlaveDataSource extends AbstractRoutingDataSource {

    private static final int TURN_MAX_COUNT = 888;

    private Integer slaveCount;

    /**
     * 轮询计数,初始为-1,AtomicInteger是线程安全的
     */
    private AtomicInteger counter = new AtomicInteger(-1);

    /**
     * 读库路由键仓库
     */
    private List<Object> slaveDataSources = new ArrayList<Object>(0);

    @Override
    protected Object determineCurrentLookupKey() {
        if (DynamicDataSourceHolder.DB_MASTER.equals(DynamicDataSourceHolder.getRouteKey())) {
            Object key = DynamicDataSourceHolder.getRouteKey();
            System.out.println("当前数据源为: " + key);
            return key;
        }
        Object key = getSlaveKey();
        System.out.println("当前数据源为: " + key);
        return key;

    }

    /**
     * 初始化读库路由键仓库
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        // 反射获取父类AbstractRoutingDataSource中私有属性resolvedDataSources
        Field field = ReflectionUtils.findField(AbstractRoutingDataSource.class, "resolvedDataSources");
        field.setAccessible(true);

        try {
            Map<Object, DataSource> resolvedDataSources = (Map<Object, DataSource>) field.get(this);
            //数据源总数 = 读库数量 + 写库数量(这里一主多从 写库数量即为1)
            this.slaveCount = resolvedDataSources.size() - 1;
            for (Map.Entry<Object, DataSource> entry : resolvedDataSources.entrySet()) {
                if (DynamicDataSourceHolder.DB_MASTER.equals(entry.getKey())) {
                    continue;
                }
                slaveDataSources.add(entry.getKey());
            }
        } catch (Exception e) {
            System.out.println("DynamicMulSlaveDataSource -> afterPropertiesSet Exception:"+e);
        }
    }

    /**
     * 轮询算法实现
     * @return 从库路由键
     */
    private Object getSlaveKey() {
        // 获取偏移量
        Integer index = counter.incrementAndGet() % slaveCount;
        // 固定偏移量范围避免数值越界
        if (counter.get() > TURN_MAX_COUNT) {
            // 重置偏移量
            counter.set(-1);
        }
        return slaveDataSources.get(index);
    }

}
