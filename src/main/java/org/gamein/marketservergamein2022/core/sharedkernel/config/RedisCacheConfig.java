package org.gamein.marketservergamein2022.core.sharedkernel.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
public class RedisCacheConfig {
    @Bean(name = "requestLogCache")
    public Cache<String, Long> createCache() {
        return CacheBuilder
                .newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }
}