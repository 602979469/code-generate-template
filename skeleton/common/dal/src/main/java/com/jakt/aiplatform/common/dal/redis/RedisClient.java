package com.jakt.aiplatform.common.dal.redis;

import java.util.List;

/**
 * common-dal 通用 Redis KV 客户端。
 */
public interface RedisClient {

    void set(String key, Object value, long ttlSeconds);

    <T> T get(String key, Class<T> type);

    <T> List<T> multiGet(List<String> keys, Class<T> type);

    void delete(String key);
}
