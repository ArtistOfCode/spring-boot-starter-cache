package com.codeartist.component.cache;

import com.codeartist.component.cache.support.LocalCache;
import com.codeartist.component.cache.support.caffeine.CaffeineCache;
import com.codeartist.component.core.support.metric.Metrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Caffeine配置
 *
 * @author AiJiangnan
 * @date 2023-11-16
 */
@SpringBootConfiguration
@ConditionalOnClass(Caffeine.class)
public class CaffeineAutoConfiguration {

    @Bean
    @Primary
    public Cache<Object, Object> defaultCache() {
        return Caffeine.newBuilder().maximumSize(1_000).build();
    }

    @Bean
    @Primary
    public LocalCache defaultLocalCache(Cache<Object, Object> defaultCache, Metrics metrics) {
        return new CaffeineCache(defaultCache, metrics);
    }
}
