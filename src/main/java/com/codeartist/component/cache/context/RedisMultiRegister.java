package com.codeartist.component.cache.context;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.RedisMultiProperties;
import com.codeartist.component.cache.core.redis.RedisCache;
import com.codeartist.component.cache.core.redis.SpringRedisCache;
import com.codeartist.component.core.support.metric.Metrics;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * Redis多数据连接注册Bean
 *
 * @author AiJiangnan
 * @date 2023-11-14
 */
@Slf4j
public class RedisMultiRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private static final String REDIS_CACHE_BEAN_NAME = "RedisCache";
    private static final String SPRING_REDIS_PREFIX = "spring.redis";
    private static final String CONNECTION_FACTORY_BEAN_NAME = "LettuceConnectionFactory";
    private static final String REDIS_TEMPLATE_BEAN_NAME = "RedisTemplate";

    private BeanFactory beanFactory;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BindResult<RedisMultiProperties> bindResult = Binder.get(environment).bind(SPRING_REDIS_PREFIX, RedisMultiProperties.class);

        if (!bindResult.isBound()) {
            return;
        }

        RedisMultiProperties multiProperties = bindResult.get();

        if (CollectionUtils.isEmpty(multiProperties.getMulti())) {
            return;
        }

        multiProperties.getMulti().forEach((name, properties) -> {
            registerConnectionBean(registry, name, properties);
            registerTemplateBean(registry, name);
            registerRedisCacheBean(registry, name);
        });
    }

    private void registerConnectionBean(BeanDefinitionRegistry registry, String name, RedisProperties properties) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisConnectionFactory.class, () -> {
            ClientResources clientResources = beanFactory.getBean(ClientResources.class);
            RedisLettuceConnectionFactory factory = new RedisLettuceConnectionFactory(properties, clientResources);
            return factory.buildStandaloneConnectionFactory();
        });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + CONNECTION_FACTORY_BEAN_NAME, definition);
        printRegisterBeanLog(name + CONNECTION_FACTORY_BEAN_NAME, LettuceConnectionFactory.class.getName());
    }

    private void registerTemplateBean(BeanDefinitionRegistry registry, String name) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(StringRedisTemplate.class, () -> {
            LettuceConnectionFactory factory = beanFactory.getBean(name + CONNECTION_FACTORY_BEAN_NAME, LettuceConnectionFactory.class);
            return new StringRedisTemplate(factory);
        });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + REDIS_TEMPLATE_BEAN_NAME, definition);
        printRegisterBeanLog(name + REDIS_TEMPLATE_BEAN_NAME, StringRedisTemplate.class.getName());
    }

    private void registerRedisCacheBean(BeanDefinitionRegistry registry, String name) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisCache.class, () -> {
            Metrics metrics = beanFactory.getBean(Metrics.class);
            CacheProperties cacheProperties = beanFactory.getBean(CacheProperties.class);
            StringRedisTemplate redisTemplate = beanFactory.getBean(name + REDIS_TEMPLATE_BEAN_NAME, StringRedisTemplate.class);

            return new SpringRedisCache(redisTemplate, cacheProperties, metrics);
        });
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        registry.registerBeanDefinition(name + REDIS_CACHE_BEAN_NAME, definition);
        printRegisterBeanLog(name + REDIS_CACHE_BEAN_NAME, RedisCache.class.getName());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void printRegisterBeanLog(String beanName, String type) {
        log.info("Bean '{}' of type [{}] is registered", beanName, type);
    }
}
