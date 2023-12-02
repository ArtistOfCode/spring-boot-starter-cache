package com.codeartist.component.cache.aop;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;

/**
 * @author J.N.AI
 * @date 2023-11-21
 */
public abstract class CacheAnnotationPointcut extends StaticMethodMatcherPointcut {

    protected abstract CacheOperationSource getCacheOperationSource();

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        CacheOperationSource cas = getCacheOperationSource();
        return cas != null && !CollectionUtils.isEmpty(cas.getCacheOperations(method, targetClass));
    }
}
