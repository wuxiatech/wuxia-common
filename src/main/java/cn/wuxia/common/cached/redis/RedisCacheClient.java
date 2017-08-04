/*
* Created on :2016年4月13日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.cached.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.cached.memcached.XMemcachedClient;
import cn.wuxia.common.util.StringUtil;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisCacheClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(XMemcachedClient.class);

    private ShardedJedis jedis;

    private int expiredTime = 0;

    public void init(String... servers) {
        if (ArrayUtils.isEmpty(servers)) {
            logger.error("初始化失败，没找到redis服务端");
            return;
        }

        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        for (String server : servers) {
            String host = StringUtil.substringBefore(server, ":");
            String port = StringUtil.substringAfter(server, ":");
            shards.add(new JedisShardInfo(host, port));
        }
        ShardedJedisPool sjp = new ShardedJedisPool(new JedisPoolConfig(), shards);
        jedis = sjp.getResource();
    }

    @Override
    public boolean containKey(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        set(key, value, expiredTime);

    }

    @Override
    public void add(String key, Object value) {
        set(key, value);

    }

    @Override
    public void set(String key, Object value, int expiredTime) {
        if (value == null)
            return;
        final byte[] keyf = key.getBytes();
        final byte[] valuef = new ObjectsTranscoder().serialize(value);
        jedis.set(keyf, valuef);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null)
            return;
        final byte[] keyf = key.getBytes();
        if (value instanceof List) {
            final byte[] valuef = new ListTranscoder().serialize(value);
            jedis.set(keyf, valuef);
        } else {
            final byte[] valuef = new ObjectsTranscoder().serialize(value);
            jedis.set(keyf, valuef);
        }
    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        // TODO Auto-generated method stub
    }

    @Override
    public void replace(String key, Object value) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> T get(String key) {
        byte[] value = jedis.get(key.getBytes());
        return (T) new ObjectsTranscoder().deserialize(value);
    }

    @Override
    public long incr(String key, int by, long defaultValue) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long decr(String key, int by, long defaultValue) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void delete(String key) {
        jedis.del(key);
        jedis.del(key.getBytes());
    }

    @Override
    public void flushAll() {
    }

    @Override
    public void flushAll(String[] servers) {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdown() {
        jedis.close();
        jedis.disconnect();

    }

    @Override
    public void add(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub

    }

    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T get(String key, String namespace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String key, String namespace) {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush(String namespace) {
        // TODO Auto-generated method stub

    }

}
