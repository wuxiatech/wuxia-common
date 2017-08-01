/*
* Created on :2016年4月13日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.cached.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import cn.wuxia.common.cached.memcached.MemcachedUtils;

public class RedisCache implements Cache {

    private RedisTemplate<String, Object> redisTemplate;

    private String cacheName;

    private int expiredTime = 0;

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this.redisTemplate;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (MemcachedUtils.hasControlChar((String) key)) {
            key = key.toString().replaceAll("\\s*", "");
        }
        final String keyf = (String) key;
        Object object = null;
        object = redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                byte[] key = keyf.getBytes();
                byte[] value = connection.get(key);
                if (value == null) {
                    return null;
                }
                return toObject(value);

            }
        });
        return (object != null ? new SimpleValueWrapper(object) : null);
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null)
            return;
        if (MemcachedUtils.hasControlChar((String) key)) {
            key = key.toString().replaceAll("\\s*", "");
        }
        final String keyf = (String) key;
        final Object valuef = value;

        redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyb = keyf.getBytes();
                byte[] valueb = toByteArray(valuef);
                connection.set(keyb, valueb);
                if (expiredTime > 0) {
                    connection.expire(keyb, expiredTime);
                }
                return 1L;
            }
        });
    }

    /**
     * 描述 : <Object转byte[]>. <br>
     * <p>
     * <使用方法说明>
     * </p>
     * 
     * @param obj
     * @return
     */
    private byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 描述 : <byte[]转Object>. <br>
     * <p>
     * <使用方法说明>
     * </p>
     * 
     * @param bytes
     * @return
     */
    private Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    @Override
    public void evict(Object key) {
        final String keyf = (String) key;
        redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.del(keyf.getBytes());
            }
        });
    }

    @Override
    public void clear() {
        redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "ok";
            }
        });
    }

    @Override
    public <T> T get(Object key, Class<T> arg1) {
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
    public <T> T get(Object arg0, Callable<T> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

}
