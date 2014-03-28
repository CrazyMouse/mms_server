package com.crazymouse.mmsserver;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 下午2:04
 */
public class ConfigUtil {
    private ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<String, String>();

    public String getConfig(String name) {
        return cache.get(name.trim());
    }

    public void setConfig(String name,String value){
        cache.put(name.trim(),value.trim());
    }
}
