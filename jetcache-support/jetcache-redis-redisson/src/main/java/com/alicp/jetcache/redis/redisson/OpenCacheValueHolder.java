package com.alicp.jetcache.redis.redisson;

import java.io.Serializable;

/**
 * OpenCacheValueHolder
 *
 * @author ironman@tinman.cn Date: 2021/6/17 Time: 17:44 Description: No Description
 */
public class OpenCacheValueHolder<V> implements Serializable {
    private V value;
    private long expireTime;
    private long accessTime;

    /**
     * used by kyro
     */
    public OpenCacheValueHolder() {
    }

    public OpenCacheValueHolder(V value, long expireAfterWrite) {
        this.value = value;
        this.accessTime = System.currentTimeMillis();
        this.expireTime = accessTime + expireAfterWrite;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }
}
