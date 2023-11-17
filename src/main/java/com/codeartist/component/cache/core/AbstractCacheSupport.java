package com.codeartist.component.cache.core;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.cache.exception.CacheException;
import com.codeartist.component.core.support.metric.Metrics;
import com.codeartist.component.core.util.Assert;

/**
 * 缓存支持
 *
 * @author AiJiangnan
 * @date 2023-11-15
 */
public abstract class AbstractCacheSupport {

    private static final String CACHE_HIT_METRIC = "cache_hit";
    private static final String CACHE_MISS_METRIC = "cache_miss";

    protected final CacheType type;
    protected final Metrics metrics;

    public AbstractCacheSupport(CacheType type, Metrics metrics) {
        this.type = type;
        this.metrics = metrics;
    }

    protected void hit(String key) {
        metrics.counter(CACHE_HIT_METRIC, "type", type.name(), "key", key.split(CacheProperties.DELIMITER)[0]);
    }

    protected void miss(String key) {
        metrics.counter(CACHE_MISS_METRIC, "type", type.name(), "key", key.split(CacheProperties.DELIMITER)[0]);
    }

    protected void checkNull(Object key) {
        Assert.notNull(key, () -> new CacheException("Cache key is null."));
    }

    protected void checkKey(String key) {
        Assert.notBlank(key, () -> new CacheException("Cache key is blank."));
    }
}
