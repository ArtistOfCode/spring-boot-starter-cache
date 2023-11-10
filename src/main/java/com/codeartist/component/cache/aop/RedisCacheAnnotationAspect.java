package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheContext;
import com.codeartist.component.cache.multi.RedisMultiConnectionFactory;
import com.codeartist.component.cache.support.RedisCacheTemplate;
import com.codeartist.component.core.support.cache.annotation.Cache;
import com.codeartist.component.core.support.cache.annotation.CacheDelete;
import com.codeartist.component.core.support.cache.annotation.CacheLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.Map;

/**
 * 缓存自定义注解切片
 *
 * @author 艾江南
 * @since 2018-11-07
 */
@Aspect
@Order(2)
@RequiredArgsConstructor
public class RedisCacheAnnotationAspect extends AbstractCacheAnnotationAspect {

    private final Map<String, RedisCacheTemplate> redisCacheTemplateMap;

    @Around("@annotation(cache)")
    public Object doAround(ProceedingJoinPoint joinPoint, Cache cache) {
        CacheContext context = new CacheContext();
        context.setKey(cache.key());
        context.setSpel(cache.spel());
        context.setTimeout(Duration.ofMillis(cache.timeUnit().toMillis(cache.timeout())));

        return super.doCache(getCacheTemplate(cache.cluster()), joinPoint, context);
    }

    @Around("@annotation(evict)")
    public Object doAfter(ProceedingJoinPoint joinPoint, CacheDelete evict) throws Throwable {
        CacheContext context = new CacheContext();
        context.setKey(evict.key());
        context.setSpel(evict.spel());

        return super.doDelete(getCacheTemplate(evict.cluster()), joinPoint, context);
    }

    /**
     * 分布式锁
     * <p>
     * NOTE：
     * <p>
     * 1. 缓存当前时间戳
     * <p>
     * 2. 缓存成功：不存在锁，执行方法后清除锁
     * <p>
     * 3. 缓存失败：存在锁，抛出业务异常
     */
    @Around("@annotation(lock)")
    public Object doAround(ProceedingJoinPoint joinPoint, CacheLock lock) throws Throwable {
        // TODO 实现分布式锁
        return joinPoint.proceed();
    }

    private RedisCacheTemplate getCacheTemplate(String cluster) {
        return redisCacheTemplateMap.get(cluster + RedisMultiConnectionFactory.CACHE_TEMPLATE_BEAN_NAME);
    }
}
