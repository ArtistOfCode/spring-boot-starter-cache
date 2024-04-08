package com.codeartist.component.cache;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.context.RedisMultiRegister;
import com.codeartist.component.cache.core.redis.RedisCache;
import com.codeartist.component.cache.core.redis.SpringRedisCache;
import com.codeartist.component.core.support.metric.Metrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author AiJiangnan
 * @date 2023-11-16
 */
@Configuration(proxyBeanMethods = false)
@Import(RedisMultiRegister.class)
@ConditionalOnClass(RedisTemplate.class)
public class RedisAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisCache defaultRedisCache(StringRedisTemplate stringRedisTemplate,
                                        CacheProperties cacheProperties,
                                        Metrics metrics) {
        return new SpringRedisCache(stringRedisTemplate, cacheProperties, metrics);
    }
}
