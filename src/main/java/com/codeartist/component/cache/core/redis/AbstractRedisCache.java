package com.codeartist.component.cache.core.redis;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.core.AbstractCache;
import com.codeartist.component.core.support.metric.Metrics;
import com.codeartist.component.core.support.serializer.TypeRef;

import java.time.Duration;

/**
 * 缓存抽象类
 *
 * @author AiJiangnan
 * @date 2021/5/25
 */
public abstract class AbstractRedisCache extends AbstractCache implements RedisCache {

    public AbstractRedisCache(CacheProperties cacheProperties, Metrics metrics) {
        super(CacheType.REDIS, cacheProperties, metrics);
    }

    protected abstract byte[] doGetExRaw(String key, Duration duration);

    protected abstract byte[] doGetRaw(String key, String hashKey);

    protected abstract void doSetRaw(String key, String hashKey, byte[] data);

    @Override
    public <T> T getEx(String key, Duration duration, Class<T> clazz) {
        checkKey(key);
        byte[] data = doGetExRaw(key, duration);
        if (data == null) {
            miss(key);
            return null;
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T getEx(String key, Duration duration, TypeRef<T> clazz) {
        checkKey(key);
        byte[] data = doGetExRaw(key, duration);
        if (data == null) {
            miss(key);
            return null;
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T getHash(String key, String hashKey, Class<T> clazz) {
        checkKey(key);
        checkKey(hashKey);
        byte[] data = doGetRaw(key, hashKey);
        if (data == null) {
            miss(key);
            return null;
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T getHash(String key, String hashKey, TypeRef<T> clazz) {
        checkKey(key);
        checkKey(hashKey);
        byte[] data = doGetRaw(key, hashKey);
        if (data == null) {
            miss(key);
            return null;
        }
        hit(key);
        return deserialize(data, clazz);
    }

    @Override
    public void setHash(String key, String hashKey, Object data) {
        doSetRaw(key, hashKey, serialize(data));
    }
}
