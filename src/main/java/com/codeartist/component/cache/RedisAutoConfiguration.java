package com.codeartist.component.cache;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.support.RedisCache;
import com.codeartist.component.cache.support.redis.SpringRedisCache;
import com.codeartist.component.core.support.metric.Metrics;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author AiJiangnan
 * @date 2023-11-16
 */
@SpringBootConfiguration
public class RedisAutoConfiguration {

    @Bean
    @Primary
    public RedisCache defaultRedisCache(StringRedisTemplate stringRedisTemplate,
                                        CacheProperties cacheProperties,
                                        Metrics metrics) {
        return new SpringRedisCache(stringRedisTemplate, cacheProperties, metrics);
    }
}
