package com.ithzk.rws.utils.dynamic;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Properties;

/**
 * 读写分离路由插件
 * @author hzk
 * @date 2019/3/26
 */
@Intercepts(
        //update 增删改 query 查询
        {@Signature(type = Executor.class,method = "update",args = {MappedStatement.class,Object.class}),
        @Signature(type = Executor.class,method = "query",args = {MappedStatement.class,Object.class, RowBounds.class, ResultHandler.class})
        }
)
public class DynamicDataSourcePlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //判断操作是否存在事务
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        //默认让routeKey为MASTER
        String routeKey = DynamicDataSourceHolder.DB_MASTER;
        //第一个参数为MappedStatement对象，第二参数为传入的参数
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];

        if(active){
            //带事务操作操作主库
            routeKey = DynamicDataSourceHolder.DB_MASTER;
        }else{
            //判断读方法
            if(mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)){
                //如果使用select_last_insert_id函数
                if(mappedStatement.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)){
                    routeKey = DynamicDataSourceHolder.DB_MASTER;
                }else{
                    routeKey = DynamicDataSourceHolder.DB_SLAVE;
                }
            }
        }
        //设置确定的路由Key
        DynamicDataSourceHolder.setRouteKey(routeKey);
        System.out.println("使用["+invocation.getMethod().getName()+"]方法,使用["+routeKey+"]策略,执行的SQL命令["+mappedStatement.getSqlCommandType().name()+"]");
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
