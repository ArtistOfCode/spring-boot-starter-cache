package com.codeartist.component.cache.multi;

import com.codeartist.component.cache.bean.RedisMultiProperties;
import com.codeartist.component.cache.support.RedisCacheTemplate;
import com.codeartist.component.cache.support.redis.RedisCacheTemplateImpl;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * Redis多数据连接注册Bean
 *
 * @author J.N.AI
 * @date 2023/7/20
 */
@Slf4j
public class RedisMultiConnectionFactory implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    public static final String DEFAULT_REDIS_NAME = "default";
    public static final String CACHE_TEMPLATE_BEAN_NAME = "RedisCacheTemplate";
    private static final String SPRING_REDIS_PREFIX = "spring.redis";
    private static final String CONNECTION_FACTORY_BEAN_NAME = "LettuceConnectionFactory";
    private static final String REDIS_TEMPLATE_BEAN_NAME = "RedisTemplate";

    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        RedisMultiProperties multiProperties = Binder.get(applicationContext.getEnvironment())
                .bind(SPRING_REDIS_PREFIX, RedisMultiProperties.class).get();

        registerConnectionBean(registry, DEFAULT_REDIS_NAME, multiProperties);
        registerTemplateBean(registry, DEFAULT_REDIS_NAME);
        registerCacheTemplateBean(registry, DEFAULT_REDIS_NAME);

        if (CollectionUtils.isEmpty(multiProperties.getMulti())) {
            return;
        }

        multiProperties.getMulti().forEach((name, properties) -> {
            registerConnectionBean(registry, name, properties);
            registerTemplateBean(registry, name);
            registerCacheTemplateBean(registry, name);
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void registerConnectionBean(BeanDefinitionRegistry registry, String name, RedisProperties properties) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisConnectionFactory.class,
                () -> {
                    ObjectProvider<RedisSentinelConfiguration> sentinelProvider =
                            applicationContext.getBeanProvider(RedisSentinelConfiguration.class);
                    ObjectProvider<RedisClusterConfiguration> clusterProvider =
                            applicationContext.getBeanProvider(RedisClusterConfiguration.class);
                    ClientResources clientResources = applicationContext.getBean(ClientResources.class);

                    RedisMultiConnectionConfiguration configuration =
                            new RedisMultiConnectionConfiguration(properties, sentinelProvider, clusterProvider);
                    return configuration.createConnectionFactory(clientResources);
                });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setPrimary(DEFAULT_REDIS_NAME.equals(name));
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + CONNECTION_FACTORY_BEAN_NAME, definition);
        log.info("Bean '{}' of type [{}] is registered", name + CONNECTION_FACTORY_BEAN_NAME, RedisConnectionFactory.class.getName());
    }

    private void registerTemplateBean(BeanDefinitionRegistry registry, String name) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StringRedisTemplate.class,
                () -> {
                    LettuceConnectionFactory factory =
                            applicationContext.getBean(name + CONNECTION_FACTORY_BEAN_NAME, LettuceConnectionFactory.class);
                    return new StringRedisTemplate(factory);
                });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setPrimary(DEFAULT_REDIS_NAME.equals(name));
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + REDIS_TEMPLATE_BEAN_NAME, definition);
        log.info("Bean '{}' of type [{}] is registered", name + REDIS_TEMPLATE_BEAN_NAME, StringRedisTemplate.class.getName());
    }

    private void registerCacheTemplateBean(BeanDefinitionRegistry registry, String name) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisCacheTemplate.class,
                () -> {
                    StringRedisTemplate redisTemplate =
                            applicationContext.getBean(name + REDIS_TEMPLATE_BEAN_NAME, StringRedisTemplate.class);

                    return new RedisCacheTemplateImpl(redisTemplate);
                });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setPrimary(DEFAULT_REDIS_NAME.equals(name));
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + CACHE_TEMPLATE_BEAN_NAME, definition);
        log.info("Bean '{}' of type [{}] is registered", name + CACHE_TEMPLATE_BEAN_NAME, RedisCacheTemplate.class.getName());
    }
}
