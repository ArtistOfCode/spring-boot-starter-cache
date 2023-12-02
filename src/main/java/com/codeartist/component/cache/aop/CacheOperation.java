package com.codeartist.component.cache.aop;

import com.codeartist.component.cache.bean.CacheAction;
import lombok.Data;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author J.N.AI
 * @date 2023-12-01
 */
@Data
public class CacheOperation {

    private CacheAction action;

    private String[] name;

    private String key;

    private long timeout;

    private TimeUnit timeUnit;

    private boolean local;

    private boolean combine;

    public Duration getDuration() {
        return Duration.ofMillis(timeUnit.toMillis(timeout));
    }
}
