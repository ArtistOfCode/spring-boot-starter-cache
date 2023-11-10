package com.codeartist.component.cache.support.redis;

import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.support.AbstractCacheTemplate;
import com.codeartist.component.cache.support.RedisCacheTemplate;
import com.codeartist.component.core.support.serializer.TypeRef;

import java.time.Duration;

/**
 * 缓存抽象类
 *
 * @author 艾江南
 * @date 2021/5/25
 */
public abstract class AbstractRedisCacheTemplate extends AbstractCacheTemplate implements RedisCacheTemplate {

    public AbstractRedisCacheTemplate() {
        super(CacheType.REDIS);
    }

    protected abstract String doGet(String key, String hashKey);

    protected abstract void doSet(String key, String hashKey, String data, Duration duration);

    @Override
    public <T> T get(String key, String hashKey, Class<T> clazz) {
        checkKey(key);
        checkKey(hashKey);
        String data = doGet(key, hashKey);
        if (data == null) {
            cacheMetrics.miss(type, key);
            return null;
        }
        cacheMetrics.hit(type, key);
        return deserialize(data, clazz);
    }

    @Override
    public <T> T get(String key, String hashKey, TypeRef<T> clazz) {
        checkKey(key);
        checkKey(hashKey);
        String data = doGet(key, hashKey);
        if (data == null) {
            cacheMetrics.miss(type, key);
            return null;
        }
        cacheMetrics.hit(type, key);
        return deserialize(data, clazz);
    }

    @Override
    public void set(String key, String hashKey, Duration duration, Object data) {
        checkKey(key);
        checkKey(hashKey);
        doSet(key, hashKey, serialize(data), duration);
    }
}
