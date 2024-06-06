package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheAction;
import com.codeartist.component.cache.core.Cache;
import com.codeartist.component.cache.core.LocalCache;
import com.codeartist.component.cache.core.redis.RedisCache;
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
import java.time.Duration;
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

        if (cacheLockOperation != null) {
            String[] name = cacheLockOperation.getName();
            String key = cacheLockOperation.getKey();
            Duration duration = cacheLockOperation.getDuration();

            Assert.state(name.length != 1,
                    exception("Cache lock name length error, lock cache must be 1."));
            RedisCache cache = ((RedisCache) getCache(name[0]));

        }


        return invokeWithCache(invocation, cacheOperation, cacheDeleteOperation);
    }

    private Object invokeWithCache(MethodInvocation invocation, CacheOperation cacheOperation, CacheOperation cacheDeleteOperation) {
        Method method = invocation.getMethod();

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
            String key = cacheOperation.getKey();
            Duration duration = cacheOperation.getDuration();

            Assert.state(cacheOperation.isCombine() ? cacheNameArr.length == 2 : cacheNameArr.length == 1,
                    exception("Cache name length error, single cache is 1, combine cache is 2."));

            String localCacheName;
            String cacheName;

            // 二级缓存
            if (cacheOperation.isCombine()) {
                localCacheName = cacheNameArr[0];
                cacheName = cacheNameArr[1];

                LocalCache localCache = getLocalCache(localCacheName);
                Cache cache = getCache(cacheName);

                returnValue = localCache.get(key, () -> cache.get(key, duration, getTypeRef(method), invoker));

            } else {
                // 一组缓存
                localCacheName = cacheNameArr[0];
                cacheName = cacheNameArr[0];

                // 本地缓存
                if (cacheOperation.isLocal()) {
                    LocalCache localCache = getLocalCache(localCacheName);
                    returnValue = localCache.get(key, invoker);
                } else {
                    Cache cache = getCache(cacheName);
                    returnValue = cache.get(key, duration, getTypeRef(method), invoker);
                }
            }
        }

        returnValue = returnValue != null ? returnValue : invoker.get();

        if (cacheDeleteOperation != null) {
            String key = cacheDeleteOperation.getKey();
            String[] cacheNameArr = cacheDeleteOperation.getName();

            Assert.state(cacheDeleteOperation.isCombine() ? cacheNameArr.length == 2 : cacheNameArr.length == 1,
                    exception("Cache name length error, single cache is 1, combine cache is 2."));

            if (cacheDeleteOperation.isCombine() || cacheDeleteOperation.isLocal()) {
                String localCacheName = cacheNameArr[0];
                LocalCache localCache = getLocalCache(localCacheName);
                localCache.delete(key);
            }

            if (cacheDeleteOperation.isCombine() || !cacheDeleteOperation.isLocal()) {
                String cacheName = cacheDeleteOperation.isCombine() ? cacheNameArr[1] : cacheNameArr[0];
                Cache cache = getCache(cacheName);
                cache.delete(key);
            }
        }

        return returnValue;
    }

    private LocalCache getLocalCache(String localCacheName) {
        LocalCache localCache = localCacheMap.get(localCacheName);
        Assert.notNull(localCache, exception("Local cache bean is null."));
        return localCache;
    }

    private Cache getCache(String cacheName) {
        Cache cache = cacheMap.get(cacheName);
        Assert.notNull(cache, exception("Cache bean is null."));
        return cache;
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
