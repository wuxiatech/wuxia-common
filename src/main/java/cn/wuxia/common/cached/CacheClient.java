package cn.wuxia.common.cached;

public interface CacheClient {

    boolean containKey(String key);

    void add(String key, Object value, int expiredTime);

    void add(String key, Object value);

    void set(String key, Object value, int expiredTime);

    void set(String key, Object value);

    void replace(String key, Object value, int expiredTime);

    void replace(String key, Object value);

    public <T> T get(String key);

    public long incr(String key, int by, long defaultValue);

    public long decr(String key, int by, long defaultValue);

    void delete(String key);

    void flushAll();

    void flushAll(String[] servers);

    void init(String... servers);
    
    void shutdown();
}
