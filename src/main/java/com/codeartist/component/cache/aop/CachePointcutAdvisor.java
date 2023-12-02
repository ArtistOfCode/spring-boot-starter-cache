package com.codeartist.component.cache.aop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author J.N.AI
 * @date 2023-11-21
 */
@Getter
@Setter
public class CachePointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private CacheOperationSource cacheOperationSource;

    private Pointcut pointcut = new CacheAnnotationPointcut() {
        @Override
        protected CacheOperationSource getCacheOperationSource() {
            return cacheOperationSource;
        }
    };
}
