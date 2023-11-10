package com.codeartist.component.cache.metric;

import com.codeartist.component.cache.bean.CacheProperties;
import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.core.support.metric.Metrics;

/**
 * 缓存指标
 *
 * @author 艾江南
 * @date 2021/5/25
 */
public class CacheMetrics {

    private static final String CACHE_HIT_METRIC = "cache_hit";
    private static final String CACHE_MISS_METRIC = "cache_miss";

    private final Metrics metrics;

    public CacheMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 缓存命中数
     */
    public void hit(CacheType type, String key) {
        record(CACHE_HIT_METRIC, type, key);
    }

    /**
     * 缓存未命中数
     */
    public void miss(CacheType type, String key) {
        record(CACHE_MISS_METRIC, type, key);
    }

    private void record(String name, CacheType type, String key) {
        metrics.counter(name, "type", type.name(), "key", key.split(CacheProperties.DELIMITER)[0]);
    }
}
