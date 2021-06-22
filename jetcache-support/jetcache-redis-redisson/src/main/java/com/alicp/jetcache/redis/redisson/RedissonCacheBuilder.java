package com.alicp.jetcache.redis.redisson;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import org.redisson.api.RedissonClient;

/**
 * RedissonCacheBuilder
 *
 * @author ironman@tinman.cn Date: 2021/6/17 Time: 17:47 Description: No Description
 */
public class RedissonCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    private RedissonClient redissonClient;

    public static class RedissonCacheBuilderImpl extends RedissonCacheBuilder<RedissonCacheBuilderImpl> {
    }

    public static RedissonCacheBuilderImpl createRedisCacheBuilder() {
        return new RedissonCacheBuilderImpl();
    }

    protected RedissonCacheBuilder() {
        buildFunc(config -> new RedissonCache((RedissonCacheConfig) config, redissonClient));
    }

    @Override
    public RedissonCacheConfig getConfig() {
        if (config == null) {
            config = new RedissonCacheConfig();
        }
        return (RedissonCacheConfig) config;
    }

    public RedissonCacheBuilder setConfig(RedissonCacheConfig config, RedissonClient redissonClient){
        this.config = config;
        this.redissonClient = redissonClient;
        return this;
    }

}
