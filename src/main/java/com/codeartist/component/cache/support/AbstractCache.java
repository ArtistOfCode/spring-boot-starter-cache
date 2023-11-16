package com.codeartist.component.cache.support;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.core.support.metric.Metrics;
import com.codeartist.component.core.support.serializer.TypeRef;
import com.codeartist.component.core.util.JSON;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存抽象类
 *
 * @author AiJiangnan
 * @date 2021/5/25
 */
public abstract class AbstractCache extends AbstractCacheSupport implements Cache {

    private final CacheProperties cacheProperties;

    public AbstractCache(CacheType type, CacheProperties cacheProperties, Metrics metrics) {
        super(type, metrics);
        this.cacheProperties = cacheProperties;
    }

    protected abstract byte[] doGetRaw(String key);

    protected abstract void doSetRaw(String key, byte[] data, Duration duration);

    @Override
    public <T> T get(String key, Duration duration, Class<T> clazz, Supplier<T> valueLoader) {
        checkKey(key);
        byte[] data = doGetRaw(key);
        if (data == null) {
            miss(key);
            if (valueLoader != null) {
                T obj = valueLoader.get();
                doSetNull(key, obj, duration);
                return obj;
            } else {
                return null;
            }
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T get(String key, Duration duration, TypeRef<T> clazz, Supplier<T> valueLoader) {
        checkKey(key);
        byte[] data = doGetRaw(key);
        if (data == null) {
            miss(key);
            if (valueLoader != null) {
                T obj = valueLoader.get();
                doSetNull(key, obj, duration);
                return obj;
            } else {
                return null;
            }
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public void set(String key, Object data, Duration duration) {
        doSetNull(key, data, duration);
    }


    protected byte[] serialize(Object data) {
        return JSON.toJSONString(data).getBytes(StandardCharsets.UTF_8);
    }

    protected <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    protected <T> T deserialize(byte[] data, TypeRef<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    private void doSetNull(String key, Object data, Duration duration) {
        if (data == null) {
            duration = cacheProperties.getNullTimeout();
        }
        doSetRaw(key, serialize(data), duration);
    }
}
