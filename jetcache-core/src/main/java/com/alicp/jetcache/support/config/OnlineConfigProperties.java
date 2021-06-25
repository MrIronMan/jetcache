package com.alicp.jetcache.support.config;

import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * online config properties
 *
 * @author ironman@tinman.cn Date: 2021/6/24 Time: 15:41 Description: No Description
 */

@Data
@Configuration
public class OnlineConfigProperties {

    private static OnlineConfigProperties INSTANCE;

    @Value("${jetcache.onlineConfigProperties.autoDelayExpire.enabled:false}")
    private Boolean autoDelayExpire;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static OnlineConfigProperties getInstance() {
        return INSTANCE;
    }

}
