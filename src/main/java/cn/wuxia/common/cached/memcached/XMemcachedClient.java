package cn.wuxia.common.cached.memcached;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import net.rubyeye.xmemcached.CASOperation;
import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * 
 * [ticket id]
 * Description of the class 
 * @author songlin.li
 * @ Version : V<Ver.No> <2012年8月30日>
 */
public class XMemcachedClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(XMemcachedClient.class);

    private MemcachedClient memcachedClient;

    private int expiredTime = 0;

    /**
     * 从缓存中获取值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            MemcachedUtils.validateKey(key);
            return (T) memcachedClient.get(key);
        } catch (RuntimeException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (TimeoutException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (InterruptedException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (Exception e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        }
    }

    /**
     * 添加到缓存，不允许重复key，如重复则报错
     * 
     * @param key
     * @param value
     * @param expiredTime  缓存失效时间单位秒
     * @return
     */
    public void add(String key, Object value, int expiredTime) {
        if (value == null) {
            return;
        }
        boolean isadd = false;
        try {
            MemcachedUtils.validateKey(key);
            isadd = memcachedClient.add(key, expiredTime, value);
        } catch (TimeoutException e) {
            logger.warn("add from memcached server fail,key is " + key, e);
        } catch (InterruptedException e) {
            logger.warn("add from memcached server fail,key is " + key, e);
        } catch (Exception e) {
            logger.warn("add from memcached server fail,key is " + key, e);
        }
        if (!isadd) {
            logger.warn("添加{}缓存失败。", key);
        }
    }

    /**
     * 添加到缓存，不允许重复key，如重复则报错
     */
    public void add(String key, Object value) {
        add(key, value, expiredTime);
    }

    /**
     * 增加一个缓存，如key存在则替换原来的值
     * 
     * @param key
     * @param value
     * @param expiredTime 缓存失效时间单位秒
     * @return
     */
    public void set(String key, Object value, int expiredTime) {
        if (value == null) {
            logger.warn("key[{}] --> value can't be null");
            return;
        }
        boolean isset = false;
        try {
            MemcachedUtils.validateKey(key);
            isset = memcachedClient.set(key, expiredTime, value);
        } catch (TimeoutException e) {
            logger.warn("Set from memcached server fail,key is " + key, e);
        } catch (InterruptedException e) {
            logger.warn("Set from memcached server fail,key is " + key, e);
        } catch (Exception e) {
            logger.warn("Set from memcached server fail,key is " + key, e);
        }
        if (!isset) {
            logger.warn("添加{}缓存失败。", key);
        }
    }

    /**
     * 增加一个缓存，如key存在则替换原来的值,返回true
     */
    public void set(String key, Object value) {
        set(key, value, expiredTime);
    }

    /**
     * 替换一个缓存，如果缓存key存在则替换并返回true，如果不存在则不替换并返回false
     * @param key
     * @param value
     * @param expiredTime
     * @return
     */
    public void replace(String key, Object value, int expiredTime) {
        if (value == null) {
            return;
        }
        boolean isreplace = false;
        try {
            MemcachedUtils.validateKey(key);
            isreplace = memcachedClient.replace(key, expiredTime, value);
        } catch (TimeoutException e) {
            logger.warn("replace from memcached server fail,key is " + key, e);
        } catch (InterruptedException e) {
            logger.warn("replace from memcached server fail,key is " + key, e);
        } catch (Exception e) {
            logger.warn("replace from memcached server fail,key is " + key, e);
        }
        if (!isreplace) {
            logger.warn("替换{}缓存失败。", key);
        }
    }

    /**
     * 替换一个缓存，如果缓存key存在则替换并返回true，如果不存在则不替换并返回false
     */
    public void replace(String key, Object value) {

        replace(key, value, expiredTime);

    }

    /**
     * 删除一个缓存数据
     */
    public void delete(String key) {
        boolean isdelete = false;
        try {
            MemcachedUtils.validateKey(key);
            isdelete = memcachedClient.delete(key);
        } catch (TimeoutException e) {
            logger.warn("Delete from memcached server fail,key is " + key, e);
        } catch (InterruptedException e) {
            logger.warn("Delete from memcached server fail,key is " + key, e);
        } catch (Exception e) {
            logger.warn("Delete from memcached server fail,key is " + key, e);
        }
        if (!isdelete) {
            logger.warn("删除{}缓存失败。", key);
        }
    }

    /**
     * Get with the Check and Set methods, the result of the conversion type and
     * shielding exception.
     */
    @SuppressWarnings("unchecked")
    public <T> GetsResponse<T> gets(String key) {
        try {
            return (GetsResponse<T>) memcachedClient.gets(key);
        } catch (RuntimeException e) {
            logger.warn("Gets from memcached server fail,key is" + key, e);
            return null;
        } catch (TimeoutException e) {
            logger.warn("Gets from memcached server fail,key is" + key, e);
            return null;
        } catch (InterruptedException e) {
            logger.warn("Gets from memcached server fail,key is" + key, e);
            return null;
        } catch (Exception e) {
            logger.warn("Gets from memcached server fail,key is" + key, e);
            return null;
        }
    }

    /**
     * Check and Set method.
     */
    public <T> Boolean cas(String key, long casId, Object value) {
        try {
            MemcachedUtils.validateKey(key);
            return memcachedClient.cas(key, 0, new CASOperation<Integer>() {
                public int getMaxTries() {
                    return 1;
                }

                public Integer getNewValue(long currentCAS, Integer currentValue) {
                    return 2;
                }

            });
        } catch (TimeoutException e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        } catch (InterruptedException e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        } catch (Exception e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        }
    }

    /**
     * Incr method.
     */
    public long incr(String key, int by, long defaultValue) {
        try {
            MemcachedUtils.validateKey(key);
            return memcachedClient.incr(key, by, defaultValue);
        } catch (TimeoutException e) {
            logger.warn("incr from memcached server fail,key is" + key, e);
            return -1;
        } catch (InterruptedException e) {
            logger.warn("incr from memcached server fail,key is" + key, e);
            return -1;
        } catch (Exception e) {
            logger.warn("incr from memcached server fail,key is" + key, e);
            return -1;
        }
    }

    /**
     * Decr method.
     */
    public long decr(String key, int by, long defaultValue) {
        try {
            MemcachedUtils.validateKey(key);
            return memcachedClient.decr(key, by, defaultValue);
        } catch (TimeoutException e) {
            logger.warn("Decr from memcached server fail,key is" + key, e);
            return -1;
        } catch (InterruptedException e) {
            logger.warn("Decr from memcached server fail,key is" + key, e);
            return -1;
        } catch (Exception e) {
            logger.warn("Decr from memcached server fail,key is" + key, e);
            return -1;
        }
    }

    public void flushAll() {
        try {
            memcachedClient.flushAll();
        } catch (TimeoutException e) {
            logger.warn(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public void flushAll(String server) {
        try {
            memcachedClient.flushAll(AddrUtil.getOneAddress(server));
        } catch (TimeoutException e) {
            logger.warn(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public void addServer(String servers) {
        try {
            memcachedClient.addServer(servers);
        } catch (IOException e) {
            logger.warn(" Add Server error:" + e.getMessage(), e);
        }
    }

    public void removeServer(String servers) {
        try {
            memcachedClient.removeServer(servers);
        } catch (Exception e) {
            logger.warn(" Add Server error:" + e.getMessage(), e);
        }
    }

    @Override
    public boolean containKey(String key) {
        if (get(key) != null)
            return true;
        return false;
    }

    @Override
    public void flushAll(String[] servers) {
        for (String server : servers) {
            flushAll(server);
        }
    }

    /**
     * @param MemcachedClient
     */
    public void setMemcachedClient(net.rubyeye.xmemcached.MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public net.rubyeye.xmemcached.MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    @Override
    public void init(String... addrss) {
        if (ArrayUtils.isEmpty(addrss)) {
            throw new MemcachedException("初始化失败，没找到memcached服务端");
        }
        List<String> addrs = ListUtil.arrayToList(addrss);
        List<Integer> weights = Lists.newArrayList();
        for (int i = 0; i < addrs.size(); i++) {
            weights.add(i + 1);
        }
        try {
            // addrs, weights
            MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddressMap(StringUtil.join(addrs, " ")));
            memcachedClient = builder.build();
            builder.setFailureMode(true);
        } catch (IOException e1) {
            throw new MemcachedException("初始化失败", e1.getMessage());
        }
    }

    public static void main(String[] args) {
        String key = "access_token";
        key += "wx8c625ac17367e886";
        System.out.println(key);
        System.out.println(MemcachedUtils.shaKey(key));

        XMemcachedClient mc = new XMemcachedClient();
        TestMemcachedServer ms = new TestMemcachedServer();
        ms.start("127.0.0.1", 11211);
        mc.init("127.0.0.1:11211");
        // String key = "hello_" + 1;
        //        mc.set("abc", "1323", 60 * 60 * 1);
        //        String result = mc.get("abc");
        //        logger.debug("1:" + result);
        //        mc.delete("abcd");
        //        result = mc.get("abc");
        //        logger.debug("2:" + result);
        mc.delete(MemcachedUtils.shaKey("access_token"));
        System.out.println("======" + mc.get(MemcachedUtils.shaKey("access_token")));
        // mc.set(key, "999999999999999999999");
        // System.out.println("dm ********************hello_" + mc.get(key));
        // XMemcachedClient mc1 = new XMemcachedClient();
        // mc1.init(ad1);
        // XMemcachedClient mc2 = new XMemcachedClient();
        // mc2.init(ad2);
        // XMemcachedClient mc3 = new XMemcachedClient(ad3);
        // System.out.println(ad1+" ********************hello_" + mc1.get(key));
        // System.out.println(ad2+" ********************hello_" + mc2.get(key));
        // System.out.println(ad3+" ********************hello_" + mc3.get(key));
        // String addrs = ad3 + "," + ad2;
        // int[] a = new int[2];
        // a[0] = 1;
        // a[1] = 2;
        // MemcachedClientBuilder builder = new XMemcachedClientBuilder(
        // AddrUtil.getAddressMap(StringUtil.join(addrs, " ")), a);
        try {
            // MemcachedClient memcachedClient = builder.build();
            // System.out.println(builder.isFailureMode());
            // builder.setFailureMode(true);

            // memcachedClient.set("key1", 0, "123");
            // memcachedClient.set("key2", 0, "456");
            // System.out.println(memcachedClient.getStateListeners());
            // System.out.println(memcachedClient.getStats());
            // System.out.println(memcachedClient.getServersDescription());
            // close memcached client
            // System.out.println("======================"+memcachedClient.get("key1"));
            // System.out.println("======================"+memcachedClient.get("key2"));
            // memcachedClient.shutdown();
            mc.memcachedClient.shutdown();
            // mc2.memcachedClient.shutdown();
            // mc3.memcachedClient.shutdown();
        } catch (Exception e) {
            System.err.println("Shutdown MemcachedClient fail");
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {
        try {
            memcachedClient.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
