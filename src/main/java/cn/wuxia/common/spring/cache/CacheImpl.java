/*
 * Created on :31 Aug, 2015
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.spring.cache;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.cached.memcached.MemcachedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

/**
 * [ticket id]
 * 重写实现spring @Cacheable 支持 Memcache, Redis等
 *
 * @author songlin
 * @ Version : V<Ver.No> <31 Aug, 2015>
 */
public class CacheImpl implements Cache {
    private static Logger logger = LoggerFactory.getLogger(CacheImpl.class);

    private CacheClient cacheClient;

    private String cacheName;

    private int expiredTime = 0;

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public void setCacheClient(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this.cacheClient;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (MemcachedUtils.hasControlChar((String) key)) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("get:{}, cachename:{}", key, this.cacheName);

        Object object = this.cacheClient.get((String) key, this.cacheName);
        return (object != null ? new SimpleValueWrapper(object) : null);
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null)
            return;
        if (MemcachedUtils.hasControlChar((String) key)) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("set:{}, cachename:{}", key, this.cacheName);
        this.cacheClient.set((String) key, value, this.expiredTime, this.cacheName);
    }

    @Override
    public void evict(Object key) {
        if (MemcachedUtils.hasControlChar((String) key)) {
            key = key.toString().replaceAll("\\s*", "");
        }
        logger.debug("delete:{}, cachename:{}", key, this.cacheName);
        this.cacheClient.delete((String) key, this.cacheName);
    }

    @Override
    public void clear() {
        this.cacheClient.flush(this.cacheName);
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper valueWrapper = get(key);
        if (valueWrapper != null)
            return (T) valueWrapper.get();
        return null;
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper v = get(key);
        if (v == null) {
            put(key, value);
            return new SimpleValueWrapper(value);
        } else
            return v;
    }

    @Override
    public <T> T get(Object key, Callable<T> arg1) {
        return (T) get((String) key);
    }

}
