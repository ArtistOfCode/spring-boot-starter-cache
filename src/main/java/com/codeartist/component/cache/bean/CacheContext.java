package com.codeartist.component.cache.bean;

import lombok.Data;

import java.time.Duration;

/**
 * @author J.N.AI
 * @date 2023/7/18
 */
@Data
public class CacheContext {

    private String key;
    private String spel;
    private Duration timeout;
}
