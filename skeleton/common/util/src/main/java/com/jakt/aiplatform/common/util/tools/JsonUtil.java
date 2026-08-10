package com.jakt.aiplatform.common.util.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * JSON 工具：统一 ObjectMapper，供生成代码的 Convertor 做 json / jsonArray / jsonObject 转换。
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    /**
     * 对象序列化为 JSON 字符串；null 返回 null。
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 序列化失败", e);
        }
    }

    /**
     * 解析 JSON 数组（元素为简单类型/POJO）。
     *
     * @param json JSON 字符串
     * @param elementType 元素类型
     * @param <T> 元素类型
     * @return 数组列表
     */
    public static <T> List<T> parseArray(String json, Class<T> elementType) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 数组解析失败", e);
        }
    }

    /**
     * 解析 JSON 数组（元素含泛型，如 Map/String）。
     *
     * @param json JSON 字符串
     * @param type 完整数组类型
     * @param <T> 数组类型
     * @return 数组列表
     */
    public static <T> List<T> parseArray(String json, TypeReference<List<T>> type) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 数组解析失败", e);
        }
    }

    /**
     * 解析 JSON 对象（目标为简单类型/POJO）。
     *
     * @param json JSON 字符串
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 对象
     */
    public static <T> T parseObject(String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 对象解析失败", e);
        }
    }

    /**
     * 解析 JSON 对象（目标含泛型）。
     *
     * @param json JSON 字符串
     * @param type 完整目标类型
     * @param <T> 目标类型
     * @return 对象
     */
    public static <T> T parseObject(String json, TypeReference<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 对象解析失败", e);
        }
    }

    /**
     * 解析 JSON 对象为 Map。
     *
     * @param json JSON 字符串
     * @return Map
     */
    public static Map<String, Object> parseMap(String json) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 对象解析失败", e);
        }
    }
}
