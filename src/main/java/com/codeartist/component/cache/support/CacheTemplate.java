package com.codeartist.component.cache.support;

import com.codeartist.component.core.support.serializer.TypeRef;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存接口
 *
 * @author 艾江南
 * @date 2021/5/24
 */
public interface CacheTemplate {

    // 函数式缓存

    <T> T cache(String key, Duration duration, Class<T> clazz, Supplier<T> supplier);

    <T> T cache(String key, Duration duration, TypeRef<T> clazz, Supplier<T> supplier);

    // 读缓存

    <T> T get(String key, Class<T> clazz);

    <T> T get(String key, TypeRef<T> clazz);

    // 写缓存

    void set(String key, Object data);

    void set(String key, Object data, Duration duration);

    // 删缓存

    void delete(String key);

    // 判断缓存是否存在

    boolean exist(String key);
}
