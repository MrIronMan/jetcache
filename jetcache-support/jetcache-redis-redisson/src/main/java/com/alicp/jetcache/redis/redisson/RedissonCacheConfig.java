package com.alicp.jetcache.redis.redisson;

import com.alicp.jetcache.external.ExternalCacheConfig;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

/**
 * RedissonCacheConfig
 *
 * @author ironman@tinman.cn Date: 2021/6/17 Time: 17:41 Description: No Description
 */
public class RedissonCacheConfig extends ExternalCacheConfig {

    private String adress;
    private int database;
    private String password;
    private int connectionPoolSize;
    private Codec codec;

    private Config config;

    public RedissonCacheConfig() {
    }

    public RedissonCacheConfig(Config config) {
        this.config = config;
    }

    public RedissonCacheConfig(String adress, int database, String password, int connectionPoolSize, Codec codec) {
        this.adress = adress;
        this.database = database;
        this.password = password;
        this.connectionPoolSize = connectionPoolSize;
        this.codec = codec;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public Codec getCodec() {
        return codec;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }
}
