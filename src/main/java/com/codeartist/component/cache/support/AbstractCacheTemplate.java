package com.codeartist.component.cache.support;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.exception.CacheException;
import com.codeartist.component.cache.metric.CacheMetrics;
import com.codeartist.component.cache.trace.CacheTraces;
import com.codeartist.component.core.support.serializer.TypeRef;
import com.codeartist.component.core.util.Assert;
import com.codeartist.component.core.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存抽象类
 *
 * @author 艾江南
 * @date 2021/5/25
 */
public abstract class AbstractCacheTemplate implements CacheTemplate {

    @Autowired
    protected CacheProperties cacheProperties;
    @Autowired
    protected CacheMetrics cacheMetrics;
    @Autowired
    protected CacheTraces cacheTraces;

    protected final CacheType type;

    protected AbstractCacheTemplate(CacheType type) {
        this.type = type;
    }

    protected abstract String doGet(String key);

    /**
     * duration为Null时不过期
     */
    protected abstract void doSet(String key, String data, Duration duration);

    @Override
    public <T> T cache(String key, Duration duration, Class<T> clazz, Supplier<T> supplier) {
        checkKey(key);

        String data = doGetTrace(key);
        if (data != null) {
            cacheMetrics.hit(type, key);
            return deserialize(data, clazz);
        }

        T obj = supplier.get();

        doSetTrace(key, obj, duration);
        cacheMetrics.miss(type, key);
        return obj;
    }

    @Override
    public <T> T cache(String key, Duration duration, TypeRef<T> clazz, Supplier<T> supplier) {
        checkKey(key);

        String data = doGetTrace(key);
        if (data != null) {
            cacheMetrics.hit(type, key);
            return deserialize(data, clazz);
        }

        T obj = supplier.get();

        doSetTrace(key, obj, duration);
        cacheMetrics.miss(type, key);
        return obj;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        checkKey(key);
        String data = doGetTrace(key);
        if (data == null) {
            cacheMetrics.miss(type, key);
            return null;
        }
        cacheMetrics.hit(type, key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T get(String key, TypeRef<T> clazz) {
        checkKey(key);
        String data = doGetTrace(key);
        if (data == null) {
            cacheMetrics.miss(type, key);
            return null;
        }
        cacheMetrics.hit(type, key);
        return deserialize(data, clazz);
    }

    @Override
    public void set(String key, Object data) {
        set(key, data, null);
    }

    @Override
    public void set(String key, Object data, Duration duration) {
        checkKey(key);
        doSetTrace(key, data, duration);
    }

    // Key校验
    protected void checkKey(String key) {
        Assert.notBlank(key, () -> new CacheException("Cache key is blank."));
    }


    // 序列化
    protected String serialize(Object data) {
        return JSON.toJSONString(data);
    }

    protected <T> T deserialize(String data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    protected <T> T deserialize(String data, TypeRef<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    private Duration getNullDuration() {
        return Duration.ofSeconds(cacheProperties.getNullTimeout());
    }

    private String doGetTrace(String key) {
        return cacheTraces.get(type, key, () -> doGet(key));
    }

    private void doSetTrace(String key, Object data, Duration duration) {
        cacheTraces.set(type, key, () -> doSet(key, serialize(data), data == null ? getNullDuration() : duration));
    }
}
