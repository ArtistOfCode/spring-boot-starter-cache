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
import org.springframework.beans.factory.config.BeanDefinition;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.function.Function;

/**
 * Redis多数据连接注册Bean
 *
 * @author AiJiangnan
 * @date 2023-11-14
 */
@Slf4j
public class RedisMultiRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private static final String SPRING_REDIS_PREFIX = "spring.redis";

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
            registerBean(registry, name, StringRedisTemplate.class, n -> instanceStringRedisTemplate(properties));
            registerBean(registry, name, RedisCache.class, this::instanceRedisCache);
        });
    }

    private StringRedisTemplate instanceStringRedisTemplate(RedisProperties properties) {
        ClientResources clientResources = beanFactory.getBean(ClientResources.class);
        RedisLettuceConnectionFactory factory = new RedisLettuceConnectionFactory(properties, clientResources);
        return new StringRedisTemplate(factory.buildStandaloneConnectionFactory());
    }

    private RedisCache instanceRedisCache(String name) {
        Metrics metrics = beanFactory.getBean(Metrics.class);
        CacheProperties cacheProperties = beanFactory.getBean(CacheProperties.class);
        StringRedisTemplate redisTemplate = beanFactory
                .getBean(name + StringRedisTemplate.class.getSimpleName(), StringRedisTemplate.class);
        return new SpringRedisCache(redisTemplate, cacheProperties, metrics);
    }

    private <T> void registerBean(BeanDefinitionRegistry registry, String name, Class<T> beanClass,
                                  Function<String, T> instanceFunction) {

        String beanName = name + beanClass.getSimpleName();

        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> instanceFunction.apply(name))
                .setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME)
                .getRawBeanDefinition();

        registry.registerBeanDefinition(beanName, definition);
        log.info("Bean '{}' of type [{}] is registered", beanName, beanClass.getName());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
