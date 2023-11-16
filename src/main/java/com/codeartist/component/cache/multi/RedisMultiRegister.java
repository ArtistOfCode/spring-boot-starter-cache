package com.codeartist.component.cache.multi;

import com.codeartist.component.cache.bean.RedisMultiProperties;
import com.codeartist.component.core.entity.enums.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Redis多数据连接注册Bean
 *
 * @author AiJiangnan
 * @date 2023-11-14
 */
@Slf4j
public class RedisMultiRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    public static final String CACHE_TEMPLATE_BEAN_NAME = "RedisCache";
    private static final String DEFAULT_REDIS_NAME = Constants.DEFAULT;
    private static final String SPRING_REDIS_PREFIX = "spring.redis";
    private static final String CONNECTION_FACTORY_BEAN_NAME = "LettuceConnectionFactory";
    private static final String REDIS_TEMPLATE_BEAN_NAME = "RedisTemplate";

    private BeanFactory beanFactory;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        RedisMultiProperties multiProperties = Binder.get(environment).bind(SPRING_REDIS_PREFIX, RedisMultiProperties.class).get();

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
