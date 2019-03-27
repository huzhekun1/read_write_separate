package com.ithzk.rws.utils.aop;

import com.ithzk.rws.utils.dynamic.DynamicDataSourceHolder;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 如果事务管理中配置了事务策略，则采用配置的事务策略中的标记了ReadOnly的方法是用Slave，其它使用Master。
 * 如果没有配置事务管理的策略，则采用方法名匹配的原则，以query、find、get开头方法用Slave，其它用Master。
 * @author hzk
 * @date 2019/3/27
 */
public class DataSourceAspect {

    private List<String> slaveMethodPattern = new ArrayList<String>();

    private static final String[] defaultSlaveMethodStart = new String[]{ "query", "find", "get","select","list","count","select" };

    private String[] slaveMethodStart;

    /**
     * 读取事务管理中的策略
     *
     * @param txAdvice
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void setTxAdvice(TransactionInterceptor txAdvice) throws Exception {
        if (txAdvice == null) {
            //未配置事务管理策略
            return;
        }
        //获取到策略配置信息
        TransactionAttributeSource transactionAttributeSource = txAdvice.getTransactionAttributeSource();
        if (!(transactionAttributeSource instanceof NameMatchTransactionAttributeSource)) {
            return;
        }
        //使用反射技术获取到NameMatchTransactionAttributeSource对象中的nameMap属性值
        NameMatchTransactionAttributeSource matchTransactionAttributeSource = (NameMatchTransactionAttributeSource) transactionAttributeSource;
        Field nameMapField = ReflectionUtils.findField(NameMatchTransactionAttributeSource.class, "nameMap");
        //设置该字段可访问(穿透)
        nameMapField.setAccessible(true);
        Map<String, TransactionAttribute> map = (Map<String, TransactionAttribute>) nameMapField.get(matchTransactionAttributeSource);

        for (Map.Entry<String, TransactionAttribute> entry : map.entrySet()) {
            //ReadOnly只读策略加入到slaveMethodPattern
            if (!entry.getValue().isReadOnly()) {
                continue;
            }
            slaveMethodPattern.add(entry.getKey());
        }
    }

    /**
     * 在进入Service方法之前执行
     * @param point 切面对象
     */
    public void before(JoinPoint point) {
        // 获取到当前执行的方法名
        String methodName = point.getSignature().getName();

        boolean isSlave = false;

        if (slaveMethodPattern.isEmpty()) {
            //当前Spring容器中没有配置事务策略，采用方法名匹配方式
            isSlave = isSlave(methodName);
        } else {
            // 使用策略规则匹配
            for (String mappedName : slaveMethodPattern) {
                if (isMatch(methodName, mappedName)) {
                    isSlave = true;
                    break;
                }
            }
        }

        if (isSlave) {
            // 标记为读库
            DynamicDataSourceHolder.setRouteKey(DynamicDataSourceHolder.DB_MASTER);
        } else {
            // 标记为写库
            DynamicDataSourceHolder.setRouteKey(DynamicDataSourceHolder.DB_SLAVE);
        }
    }

    /**
     * 判断是否为读库
     *
     * @param methodName
     * @return
     */
    private Boolean isSlave(String methodName) {
        // 方法名以query、find、get开头的方法名走从库
        return StringUtils.startsWithAny(methodName, getSlaveMethodStart());
    }

    /**
     * 通配符匹配
     *
     * Return if the given method name matches the mapped name.
     * <p>
     * The default implementation checks for "xxx*", "*xxx" and "*xxx*" matches, as well as direct
     * equality. Can be overridden in subclasses.
     *
     * @param methodName the method name of the class
     * @param mappedName the name in the descriptor
     * @return if the names match
     * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
     */
    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    /**
     * 用户指定slave的方法名前缀
     * @param slaveMethodStart
     */
    public void setSlaveMethodStart(String[] slaveMethodStart) {
        this.slaveMethodStart = slaveMethodStart;
    }

    public String[] getSlaveMethodStart() {
        if(this.slaveMethodStart == null){
            // 没有指定，使用默认
            return defaultSlaveMethodStart;
        }
        return slaveMethodStart;
    }
}
