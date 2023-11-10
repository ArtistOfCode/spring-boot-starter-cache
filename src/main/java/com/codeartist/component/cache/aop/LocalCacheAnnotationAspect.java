package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheContext;
import com.codeartist.component.cache.support.caffeine.LocalCacheTemplate;
import com.codeartist.component.core.support.cache.annotation.LocalCache;
import com.codeartist.component.core.support.cache.annotation.LocalCacheDelete;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 缓存自定义注解切片
 *
 * @author 艾江南
 * @since 2018-11-07
 */
@Aspect
@Order(1)
@RequiredArgsConstructor
public class LocalCacheAnnotationAspect extends AbstractCacheAnnotationAspect {

    private final LocalCacheTemplate localCacheTemplate;

    @Around("@annotation(cache)")
    public Object doAround(ProceedingJoinPoint joinPoint, LocalCache cache) {
        CacheContext context = new CacheContext();
        context.setKey(cache.key());
        context.setSpel(cache.spel());

        return super.doCache(localCacheTemplate, joinPoint, context);
    }

    @Around("@annotation(evict)")
    public Object doAfter(ProceedingJoinPoint joinPoint, LocalCacheDelete evict) throws Throwable {
        CacheContext context = new CacheContext();
        context.setKey(evict.key());
        context.setSpel(evict.spel());

        return super.doDelete(localCacheTemplate, joinPoint, context);
    }
}
