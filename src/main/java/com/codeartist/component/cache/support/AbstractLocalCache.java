package com.codeartist.component.cache.support;

import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.core.support.metric.Metrics;

import java.util.function.Supplier;

/**
 * 缓存抽象类
 *
 * @author AiJiangnan
 * @date 2021/5/25
 */
public abstract class AbstractLocalCache extends AbstractCacheSupport implements LocalCache {

    public AbstractLocalCache(CacheType type, Metrics metrics) {
        super(type, metrics);
    }

    protected abstract <T> T doGet(String key);

    protected abstract void doSet(String key, Object data);

    @Override
    public <T> T get(String key, Supplier<T> valueLoader) {
        checkKey(key);
        ValueWrapper<T> data = doGet(key);
        if (data == null) {
            miss(key);
            if (valueLoader != null) {
                T obj = valueLoader.get();
                doSet(key, (ValueWrapper<T>) () -> obj);
                return obj;
            } else {
                return null;
            }
        }
        hit(key);
        return data.get();
    }

    @Override
    public void set(String key, Object data) {
        doSet(key, (ValueWrapper<Object>) () -> data);
    }

    @FunctionalInterface
    protected interface ValueWrapper<T> {
        T get();
    }
}
