package com.codeartist.component.cache.support.redis;

import com.codeartist.component.cache.support.RedisCacheTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存
 *
 * @author 艾江南
 * @date 2021/5/24
 */
public class RedisCacheTemplateImpl extends AbstractRedisCacheTemplate implements RedisCacheTemplate {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisCacheTemplateImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected String doGet(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    protected String doGet(String key, String hashKey) {
        return (String) stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    protected void doSet(String key, String data, Duration duration) {
        if (duration == null) {
            stringRedisTemplate.opsForValue().set(key, data);
        } else {
            stringRedisTemplate.opsForValue().set(key, data, duration);
        }
    }

    @Override
    protected void doSet(String key, String hashKey, String data, Duration duration) {
        stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.opsForHash().put((K) key, hashKey, data);
                operations.expire((K) key, duration.toMillis(), TimeUnit.MILLISECONDS);
                return null;
            }
        });
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public void delete(String key, Object... hashKey) {
        stringRedisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public void expire(String key, Duration duration) {
        checkKey(key);
        stringRedisTemplate.expire(key, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean exist(String key) {
        checkKey(key);
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    @Override
    public long inc(String key) {
        Long increment = stringRedisTemplate.opsForValue().increment(key);
        return increment == null ? 0L : increment;
    }
}
