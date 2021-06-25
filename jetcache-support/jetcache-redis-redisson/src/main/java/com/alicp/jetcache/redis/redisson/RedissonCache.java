package com.alicp.jetcache.redis.redisson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.FastjsonValueDecoder;
import com.alicp.jetcache.support.FastjsonValueEncoder;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisAuthRequiredException;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis redisson cache
 *
 * @author ironman@tinman.cn Date: 2021/6/17 Time: 17:37 Description: No Description
 */
public class RedissonCache<K, V> extends AbstractExternalCache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(RedissonCache.class);

    private RedissonCacheConfig config;

    /**
     * 共用项目中的 redisson 客户端
     */
    private RedissonClient redissonClient;

    Function<Object, byte[]> valueEncoder;
    Function<byte[], Object> valueDecoder;

    public RedissonCache(RedissonCacheConfig config, RedissonClient redissonClient) {
        super(config);
        this.config = config;

        this.valueEncoder = FastjsonValueEncoder.INSTANCE;
        this.valueDecoder = FastjsonValueDecoder.INSTANCE;

        Config redissonConfig = new Config();
        SingleServerConfig singleConfig = redissonConfig.useSingleServer();
        singleConfig.setAddress(config.getAdress());
        singleConfig.setDatabase(config.getDatabase());
        singleConfig.setPassword(config.getPassword());
        singleConfig.setConnectionPoolSize(config.getConnectionPoolSize());
        redissonConfig.setCodec(config.getCodec());
        this.redissonClient = redissonClient;

        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }
    }


    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new IllegalArgumentException(clazz.getName());
    }

    protected String buildStrKey(K key) {
        return config.getKeyPrefix() + ":" + key.toString();
    }

    @Override
    protected CacheGetResult<V> do_GET(K key, Type valueType) {
        RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
        if (rb.get() != null) {
//            OpenCacheValueHolder<V> holder = decode(rb.get());
//            if (System.currentTimeMillis() >= holder.getExpireTime()) {
//                return CacheGetResult.EXPIRED_WITHOUT_MSG;
//            }
            CacheValueHolder cacheValueHolder = new CacheValueHolder();
            cacheValueHolder.setValue(decode(rb.get(), valueType));
            long timeMillis = System.currentTimeMillis();
            cacheValueHolder.setAccessTime(timeMillis);
            cacheValueHolder.setExpireTime(timeMillis + config().getExpireAfterWriteInMillis());
            return new CacheGetResult(CacheResultCode.SUCCESS, null, cacheValueHolder);
        } else {
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys, Type valueType) {
        Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
        keys.forEach(key -> {
            RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
            if (rb.get() != null) {
                CacheValueHolder cacheValueHolder = new CacheValueHolder();
                cacheValueHolder.setValue(decode(rb.get(), valueType));
                long timeMillis = System.currentTimeMillis();
                cacheValueHolder.setAccessTime(timeMillis);
                cacheValueHolder.setExpireTime(timeMillis + config().getExpireAfterWriteInMillis());
                CacheGetResult<V> r = new CacheGetResult<V>(CacheResultCode.SUCCESS, null, cacheValueHolder);
                resultMap.put(key, r);
            } else {
                resultMap.put(key, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
            }
        });
        return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
    }


    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
        rb.set(encode(value), expireAfterWrite, TimeUnit.MILLISECONDS);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    private V decode(String data, Type valueType) {
        return JSONObject.parseObject(data, valueType);
    }

    private String encode(V value) {
        return JSON.toJSONString(value);
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        RBatch batch = redissonClient.createBatch();
        map.forEach((k, v) -> {
//            OpenCacheValueHolder<V> holder = new OpenCacheValueHolder(v, timeUnit.toMillis(expireAfterWrite));
            batch.getBucket(buildStrKey(k)).setAsync(encode(v), expireAfterWrite, TimeUnit.MILLISECONDS);
        });
        batch.execute();
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
        // 阿里云暂不支持这种方式
        //RBinaryStream rb = redissonClient.getBinaryStream(buildStrKey(key));
        rb.delete();
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> {
            batch.getBucket(buildStrKey(key)).deleteAsync();
//            batch.getBucket(buildStrKey(key)).deleteAsync();
        });
        batch.execute();
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
        boolean success = rb.trySet(encode(value), expireAfterWrite, TimeUnit.MILLISECONDS);
        if (success) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return CacheResult.EXISTS_WITHOUT_MSG;
        }
    }

    @Override
    protected boolean needLogStackTrace(Throwable e) {
        if (e instanceof RedisAuthRequiredException) {
            return false;
        }
        return true;
    }

    @Override
    public CacheResult tryAutoDelayExpire(K key, long expireAfterWrite, TimeUnit timeUnit) {
        RBucket<String> rb = redissonClient.getBucket(buildStrKey(key));
        rb.expire(timeUnit.toMillis(expireAfterWrite), timeUnit);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }
}
