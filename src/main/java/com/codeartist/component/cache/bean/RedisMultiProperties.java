package com.codeartist.component.cache.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import java.util.Map;

/**
 * @author J.N.AI
 * @date 2023/7/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RedisMultiProperties extends RedisProperties {

    private Map<String, RedisProperties> multi;
}
