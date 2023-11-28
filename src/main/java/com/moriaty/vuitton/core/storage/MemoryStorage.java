package com.moriaty.vuitton.core.storage;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 内存存储
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午7:17
 */
@Slf4j
public class MemoryStorage {

    private static final Map<String, MemoryItem> MEMORY_STORAGE = new HashMap<>();

    private static final long DEFAULT_SURVIVAL_SECOND = 60L;
    private static final long FOREVER_SURVIVAL_SECOND = -1L;

    private MemoryStorage() {

    }

    public static void initMemoryStorage() {
        Thread.ofVirtual().name("MemoryStorage-").start(() -> {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<String, MemoryItem> entry : MEMORY_STORAGE.entrySet()) {
                if (entry.getValue().getSurvivalSecond() == FOREVER_SURVIVAL_SECOND) {
                    continue;
                }
                if (currentTime - entry.getValue().getSaveTimeStamp()
                    > entry.getValue().getSurvivalSecond() * 1000) {
                    MEMORY_STORAGE.remove(entry.getKey());
                }
            }
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void put(String key, String value) {
        put(key, value, DEFAULT_SURVIVAL_SECOND);
    }


    public static <T> void put(String key, T value) {
        put(key, JSON.toJSONString(value, JSONWriter.Feature.WriteMapNullValue), DEFAULT_SURVIVAL_SECOND);
    }

    public static void putForever(String key, String value) {
        put(key, value, FOREVER_SURVIVAL_SECOND);
    }


    public static <T> void putForever(String key, T value) {
        put(key, JSON.toJSONString(value, JSONWriter.Feature.WriteMapNullValue), FOREVER_SURVIVAL_SECOND);
    }

    public static <T> void put(String key, T value, long survivalSecond) {
        put(key, JSON.toJSONString(value, JSONWriter.Feature.WriteMapNullValue), survivalSecond);
    }

    public static void put(String key, String value, long survivalSecond) {
        MEMORY_STORAGE.put(key, new MemoryItem()
                .setKey(key)
                .setValue(value)
                .setSaveTimeStamp(System.currentTimeMillis())
                .setSurvivalSecond(survivalSecond));
    }

    public static String get(String key) {
        return MEMORY_STORAGE.containsKey(key) ? MEMORY_STORAGE.get(key).getValue() : null;
    }

    public static <T> T get(String key, Class<T> clazz) {
        String value = get(key);
        return value != null ? JSON.parseObject(value, clazz) : null;
    }

    public static <T> T get(String key, TypeReference<T> typeReference) {
        String value = get(key);
        return value != null ? JSON.parseObject(value, typeReference) : null;
    }


    public static Map<String, MemoryItem> snapshot() {
        return Map.copyOf(MEMORY_STORAGE);
    }
}
