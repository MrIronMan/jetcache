package com.alicp.jetcache.autoconfigure;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.redisson.RedissonCacheBuilder;
import com.alicp.jetcache.redis.redisson.RedissonCacheConfig;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonAutoConfiguration
 *
 * @author ironman@tinman.cn Date: 2021/6/16 Time: 22:47 Description: No Description
 */

@Configuration
@Conditional(RedissonAutoConfiguration.RedissonCondition.class)
public class RedissonAutoConfiguration {

    public static class RedissonCondition extends JetCacheCondition {

        public RedissonCondition() {
            super("redis.redisson");
        }
    }

    @Bean
    public RedissonAutoInit redissonAutoInit() {
        return new RedissonAutoInit();
    }

    public static class RedissonAutoInit extends ExternalCacheAutoInit implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        public RedissonAutoInit() {
            super("redis.redisson");
        }

        @Override
        protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            RedissonClient redissonClient = applicationContext.getBean(RedissonClient.class);
            RedissonCacheConfig redissonCacheConfig = new RedissonCacheConfig(redissonClient.getConfig());
            ExternalCacheBuilder builder = RedissonCacheBuilder.createRedisCacheBuilder()
                .setConfig(redissonCacheConfig, applicationContext.getBean(RedissonClient.class));
            parseGeneralConfig(builder, ct);
            return builder;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
}
