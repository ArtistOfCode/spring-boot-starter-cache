package com.codeartist.component.cache.support;

import java.util.function.Supplier;

/**
 * 本地缓存接口
 *
 * @author AiJiangnan
 * @date 2021/5/24
 */
public interface LocalCache {

    default <T> T get(String key) {
        return get(key, null);
    }

    <T> T get(String key, Supplier<T> valueLoader);

    void set(String key, Object data);

    void delete(String key);
}
