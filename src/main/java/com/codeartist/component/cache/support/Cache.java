package com.codeartist.component.cache.support;

import com.codeartist.component.core.support.serializer.TypeRef;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存接口
 *
 * @author AiJiangnan
 * @date 2021/5/24
 */
public interface Cache {

    Duration INFINITE_DURATION = Duration.ZERO;

    default <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, null);
    }

    default <T> T get(String key, TypeRef<T> clazz) {
        return get(key, clazz, null);
    }

    default <T> T get(String key, Class<T> clazz, Supplier<T> valueLoader) {
        return get(key, INFINITE_DURATION, clazz, valueLoader);
    }

    default <T> T get(String key, TypeRef<T> clazz, Supplier<T> valueLoader) {
        return get(key, INFINITE_DURATION, clazz, valueLoader);
    }

    <T> T get(String key, Duration duration, Class<T> clazz, Supplier<T> valueLoader);

    <T> T get(String key, Duration duration, TypeRef<T> clazz, Supplier<T> valueLoader);

    default void set(String key, Object data) {
        set(key, data, INFINITE_DURATION);
    }

    void set(String key, Object data, Duration duration);

    void delete(String key);
}
