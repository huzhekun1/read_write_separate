package com.ithzk.rws.utils;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @author hzk
 * @date 2019/3/26
 */
public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{

    private String[] encryptPropNames = {"db.master.username","db.slave.username","db.master.password","db.slave.password"};

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if(isEncryptProp(propertyName)){
            return DesUtils.decode(propertyValue);
        }else{
            return propertyValue;
        }
    }

    private boolean isEncryptProp(String propertyName){
        for (String encryptPropName:encryptPropNames) {
            if(encryptPropName.equals(propertyName)){
                return true;
            }
        }
        return false;
    }
}
