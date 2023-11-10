package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheContext;
import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.support.CacheTemplate;
import com.codeartist.component.core.support.serializer.TypeRef;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * 缓存自定义注解切片
 *
 * @author 艾江南
 * @since 2018-11-07
 */
@RequiredArgsConstructor
public class AbstractCacheAnnotationAspect {

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 缓存
     * <p>
     * NOTE：
     * <p>
     * 1. 获取方法返回值缓存
     * <p>
     * 2. 如果缓存存在，直接返回
     * <p>
     * 3. 缓存不存在，执行方法，将方法返回结果缓存后返回
     */
    protected Object doCache(CacheTemplate cacheTemplate, ProceedingJoinPoint joinPoint, CacheContext context) {

        String key = getSpelKey(joinPoint, context.getKey(), context.getSpel());
        ReturnType type = new ReturnType((MethodSignature) joinPoint.getSignature());
        ResultHandler handler = new ResultHandler(joinPoint);

        return cacheTemplate.cache(key, context.getTimeout(), type, handler);
    }

    /**
     * 清除缓存
     * NOTE：
     * 1. 方法执行完后清除缓存
     */
    protected Object doDelete(CacheTemplate cacheTemplate, ProceedingJoinPoint joinPoint, CacheContext context) throws Throwable {
        String key = getSpelKey(joinPoint, context.getKey(), context.getSpel());

        try {
            return joinPoint.proceed();
        } finally {
            cacheTemplate.delete(key);
        }
    }

    /**
     * 获取Spel键
     */
    private String getSpelKey(JoinPoint joinPoint, String key, String spelKey) {
        if (StringUtils.isEmpty(spelKey)) {
            return key;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(methodSignature.getMethod(),
                methodSignature.getMethod(), joinPoint.getArgs(), parameterNameDiscoverer);
        String value = parser.parseExpression(spelKey).getValue(evaluationContext, String.class);
        return StringUtils.isEmpty(value) ? key : key + CacheProperties.DELIMITER + value;
    }

    @RequiredArgsConstructor
    private static class ResultHandler implements Supplier<Object> {

        private final ProceedingJoinPoint joinPoint;

        @Override
        public Object get() {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RequiredArgsConstructor
    private static class ReturnType extends TypeRef<Object> {

        private final MethodSignature methodSignature;

        @Override
        public Type getType() {
            return methodSignature.getMethod().getGenericReturnType();
        }
    }
}
