package com.codeartist.component.cache.support.caffeine;

import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.support.AbstractCacheTemplate;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * 本地缓存
 *
 * @author J.N.AI
 * @date 2023/7/15
 */
public class LocalCacheTemplate extends AbstractCacheTemplate {

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    public LocalCacheTemplate() {
        super(CacheType.LOCAL);
    }

    @Override
    protected String doGet(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    protected void doSet(String key, String data, Duration duration) {
        cache.put(key, data);
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }

    @Override
    public boolean exist(String key) {
        return doGet(key) != null;
    }
}
