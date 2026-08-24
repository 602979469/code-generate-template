package com.jakt.aiplatform.common.dal.redis;

import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 StringRedisTemplate 的通用 Redis KV 实现。
 */
@Component
public class StringRedisClient implements RedisClient {

    private final StringRedisTemplate stringRedisTemplate;

    public StringRedisClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        String json = stringRedisTemplate.opsForValue().get(key);
        return json == null ? null : JSONUtil.toBean(json, type);
    }

    @Override
    public <T> List<T> multiGet(List<String> keys, Class<T> type) {
        List<String> jsons = stringRedisTemplate.opsForValue().multiGet(keys);
        return jsons == null ? List.of()
                : jsons.stream().filter(json -> json != null).map(json -> JSONUtil.toBean(json, type)).toList();
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
