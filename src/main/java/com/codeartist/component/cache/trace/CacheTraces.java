package com.codeartist.component.cache.trace;

import com.codeartist.component.cache.bean.CacheType;
import com.codeartist.component.core.support.trace.Tracers;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author J.N.AI
 * @date 2023/8/3
 */
@RequiredArgsConstructor
public class CacheTraces {

    private final Tracers tracers;

    public String get(CacheType type, String key, Supplier<String> supplier) {
        Map<String, String> tags = Collections.singletonMap("Key", key);
        return tracers.startScopedSpan(type.name() + ":get", tags, supplier);
    }

    public void set(CacheType type, String key, Runnable runnable) {
        Map<String, String> tags = Collections.singletonMap("Key", key);
        tracers.startScopedSpan(type.name() + ":get", tags, () -> {
            runnable.run();
            return null;
        });
    }
}
