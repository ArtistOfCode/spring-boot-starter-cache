package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheAction;
import com.codeartist.component.cache.core.Cache;
import com.codeartist.component.cache.core.LocalCache;
import com.codeartist.component.cache.exception.CacheException;
import com.codeartist.component.core.support.serializer.TypeRef;
import com.codeartist.component.core.util.Assert;
import lombok.Getter;
import lombok.Setter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author J.N.AI
 * @date 2023-12-01
 */
@Getter
@Setter
public class CacheInterceptor implements MethodInterceptor {

    private CacheOperationSource cacheOperationSource;
    private Map<String, LocalCache> localCacheMap;
    private Map<String, Cache> cacheMap;

    @Override
    public Object invoke(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Map<CacheAction, CacheOperation> ops = cacheOperationSource.getCacheOperations(method, getTargetClass(target)).stream().collect(Collectors.toMap(CacheOperation::getAction, Function.identity()));

        CacheOperation cacheOperation = ops.get(CacheAction.CACHE);
        CacheOperation cacheDeleteOperation = ops.get(CacheAction.CACHE_DELETE);
        CacheOperation cacheLockOperation = ops.get(CacheAction.CACHE_LOCK);


        Supplier<Object> invoker = () -> {
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                throw new CacheException(ex);
            }
        };

        Object returnValue = null;

        if (cacheOperation != null) {

            String[] cacheNameArr = cacheOperation.getName();

            Assert.state(cacheOperation.isCombine() ? cacheNameArr.length == 2 : cacheNameArr.length == 1,
                    exception("Cache name length error, single cache is 1, combine cache is 2."));

            String localCacheName;
            String cacheName;

            if (cacheOperation.isCombine()) {
                localCacheName = cacheNameArr[0];
                cacheName = cacheNameArr[1];

                LocalCache localCache = localCacheMap.get(localCacheName);
                Assert.notNull(localCache, exception("Local cache bean is null."));

                Cache cache = cacheMap.get(cacheName);
                Assert.notNull(cache, exception("Cache bean is null."));

                returnValue = localCache.get(cacheOperation.getKey(), () ->
                        cache.get(cacheOperation.getKey(), cacheOperation.getDuration(), getTypeRef(method), invoker));

            } else {
                localCacheName = cacheNameArr[0];
                cacheName = cacheNameArr[0];

                if (cacheOperation.isLocal()) {
                    LocalCache localCache = localCacheMap.get(localCacheName);
                    Assert.notNull(localCache, exception("Local cache bean is null."));
                    returnValue = localCache.get(cacheOperation.getKey(), invoker);
                } else {
                    Cache cache = cacheMap.get(cacheName);
                    Assert.notNull(cache, exception("Cache bean is null."));
                    returnValue = cache.get(cacheOperation.getKey(), cacheOperation.getDuration(), getTypeRef(method), invoker);
                }
            }
        }

        returnValue = returnValue != null ? returnValue : invoker.get();

        if (cacheDeleteOperation != null) {
            String[] cacheNameArr = cacheDeleteOperation.getName();

            Assert.state(cacheDeleteOperation.isCombine() ? cacheNameArr.length == 2 : cacheNameArr.length == 1,
                    exception("Cache name length error, single cache is 1, combine cache is 2."));

            if (cacheDeleteOperation.isCombine() || cacheDeleteOperation.isLocal()) {
                String localCacheName = cacheNameArr[0];
                LocalCache localCache = localCacheMap.get(localCacheName);
                Assert.notNull(localCache, exception("Local cache bean is null."));
                localCache.delete(cacheDeleteOperation.getKey());
            }

            if (cacheDeleteOperation.isCombine() || !cacheDeleteOperation.isLocal()) {
                String cacheName = cacheDeleteOperation.isCombine() ? cacheNameArr[1] : cacheNameArr[0];
                Cache cache = cacheMap.get(cacheName);
                Assert.notNull(cache, exception("Cache bean is null."));
                cache.delete(cacheDeleteOperation.getKey());
            }
        }

        return returnValue;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    private TypeRef<Object> getTypeRef(Method method) {
        return new TypeRef<Object>() {
            @Override
            public Type getType() {
                return method.getGenericReturnType();
            }
        };
    }

    private Supplier<RuntimeException> exception(String message) {
        return () -> new CacheException(message);
    }
}
