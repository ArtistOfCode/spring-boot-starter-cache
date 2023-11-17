package com.codeartist.component.cache.context;

import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Redis连接工厂
 *
 * @author J.N.AI
 * @date 2023-11-17
 */
class RedisLettuceConnectionFactory {

    private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
            RedisLettuceConnectionFactory.class.getClassLoader());

    private final RedisProperties properties;
    private final ClientResources clientResources;

    public RedisLettuceConnectionFactory(RedisProperties redisProperties, ClientResources clientResources) {
        this.properties = redisProperties;
        this.clientResources = clientResources;
    }

    LettuceConnectionFactory buildStandaloneConnectionFactory() {
        return new LettuceConnectionFactory(getStandaloneConfig(), getClientConfig());
    }

    private RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(this.properties.getHost());
        config.setPort(this.properties.getPort());
        config.setUsername(this.properties.getUsername());
        config.setPassword(RedisPassword.of(this.properties.getPassword()));
        config.setDatabase(this.properties.getDatabase());
        return config;
    }

    private LettuceClientConfiguration getClientConfig() {
        LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        RedisProperties.Pool pool = this.properties.getLettuce().getPool();
        if (pool.getEnabled() != null ? pool.getEnabled() : COMMONS_POOL2_AVAILABLE) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
            }
            if (pool.getMaxWait() != null) {
                config.setMaxWait(pool.getMaxWait());
            }
            builder = LettucePoolingClientConfiguration.builder().poolConfig(config);
        }
        if (this.properties.isSsl()) {
            builder.useSsl();
        }
        if (this.properties.getTimeout() != null) {
            builder.commandTimeout(this.properties.getTimeout());
        }
        if (this.properties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = this.properties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(this.properties.getLettuce().getShutdownTimeout());
            }
        }
        if (StringUtils.hasText(this.properties.getClientName())) {
            builder.clientName(this.properties.getClientName());
        }

        builder.clientResources(clientResources);
        return builder.build();
    }
}
