package com.codeartist.component.cache.support.caffeine;

import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.support.AbstractLocalCache;
import com.codeartist.component.core.support.metric.Metrics;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * Caffeine缓存
 *
 * @author AiJiangnan
 * @date 2023/7/15
 */
public class CaffeineCache extends AbstractLocalCache {

    private final Cache<String, Object> cache;

    public CaffeineCache(Cache<String, Object> cache, Metrics metrics) {
        super(CacheType.CAFFEINE, metrics);
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doGet(String key) {
        return (T) cache.getIfPresent(key);
    }

    @Override
    protected void doSet(String key, Object data) {
        cache.put(key, data);
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }
}
