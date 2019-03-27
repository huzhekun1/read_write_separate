package com.ithzk.rws.utils.dynamic;

/**
 * @author hzk
 * @date 2019/3/26
 */
public class DynamicDataSourceHolder {

    public static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    public static final String DB_MASTER = "master";
    public static final String DB_SLAVE = "slave";

    /**
     * 获取路由Key
     * @return
     */
    public static String getRouteKey(){
        String routeKey = contextHolder.get();
        if(null == routeKey){
            routeKey = DB_MASTER;
        }
        return routeKey;
    }

    /**
     * 设置路由Key
     */
    public static void setRouteKey(String routeKey){
        contextHolder.set(routeKey);
        System.out.println("切换到数据源:"+routeKey);
    }
}
