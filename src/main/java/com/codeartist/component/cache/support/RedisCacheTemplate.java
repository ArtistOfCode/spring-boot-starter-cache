package com.codeartist.component.cache.support;

import com.codeartist.component.core.support.serializer.TypeRef;

import java.time.Duration;

/**
 * Redis缓存接口
 *
 * @author 艾江南
 * @date 2022/4/25
 */
public interface RedisCacheTemplate extends CacheTemplate {

    // Hash缓存

    <T> T get(String key, String hashKey, Class<T> clazz);

    <T> T get(String key, String hashKey, TypeRef<T> clazz);

    void set(String key, String hashKey, Duration duration, Object data);

    void delete(String key, Object... hashKey);

    // 其他操作

    void expire(String key, Duration duration);

    long inc(String key);
}
