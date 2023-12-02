package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheAction;
import com.codeartist.component.core.support.cache.annotation.Cache;
import com.codeartist.component.core.support.cache.annotation.CacheDelete;
import com.codeartist.component.core.support.cache.annotation.CacheLock;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author J.N.AI
 * @date 2023-12-01
 */
public class CacheOperationSource {

    private static final Set<Class<? extends Annotation>> CACHE_OPERATION_ANNOTATIONS = new LinkedHashSet<>(8);

    static {
        CACHE_OPERATION_ANNOTATIONS.add(Cache.class);
        CACHE_OPERATION_ANNOTATIONS.add(CacheDelete.class);
        CACHE_OPERATION_ANNOTATIONS.add(CacheLock.class);
    }

    public Collection<CacheOperation> getCacheOperations(Method method, Class<?> targetClass) {
        return parseCacheAnnotations(method);
    }

    private Collection<CacheOperation> parseCacheAnnotations(AnnotatedElement ae) {

        Collection<? extends Annotation> anns = AnnotatedElementUtils.getAllMergedAnnotations(ae, CACHE_OPERATION_ANNOTATIONS);

        if (anns.isEmpty()) {
            return null;
        }

        final Collection<CacheOperation> ops = new ArrayList<>(1);
        anns.stream().filter(ann -> ann instanceof Cache)
                .forEach(ann -> ops.add(parseCacheAnnotation((Cache) ann)));
        anns.stream().filter(ann -> ann instanceof CacheDelete)
                .forEach(ann -> ops.add(parseCacheDeleteAnnotation((CacheDelete) ann)));
        anns.stream().filter(ann -> ann instanceof CacheLock)
                .forEach(ann -> ops.add(parseCacheLockAnnotation((CacheLock) ann)));
        return ops;
    }

    private CacheOperation parseCacheAnnotation(Cache ann) {
        CacheOperation cacheOperation = new CacheOperation();
        cacheOperation.setAction(CacheAction.CACHE);
        cacheOperation.setName(ann.name());
        cacheOperation.setKey(ann.key());
        cacheOperation.setTimeout(ann.timeout());
        cacheOperation.setTimeUnit(ann.timeUnit());
        cacheOperation.setLocal(ann.local());
        cacheOperation.setCombine(ann.combine());
        return cacheOperation;
    }

    private CacheOperation parseCacheDeleteAnnotation(CacheDelete ann) {
        CacheOperation cacheOperation = new CacheOperation();
        cacheOperation.setAction(CacheAction.CACHE_DELETE);
        cacheOperation.setName(ann.name());
        cacheOperation.setKey(ann.key());
        cacheOperation.setLocal(ann.local());
        cacheOperation.setCombine(ann.combine());
        return cacheOperation;
    }

    private CacheOperation parseCacheLockAnnotation(CacheLock ann) {
        CacheOperation cacheOperation = new CacheOperation();
        cacheOperation.setAction(CacheAction.CACHE_LOCK);
        cacheOperation.setName(ann.name());
        cacheOperation.setKey(ann.key());
        cacheOperation.setTimeout(ann.timeout());
        cacheOperation.setTimeUnit(ann.timeUnit());
        return cacheOperation;
    }
}
