package com.moriaty.vuitton.core.storage;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
        Thread.ofVirtual().name("MemoryStorage-", 0).start(() -> {
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

    public static Map<String, MemoryItem> snapshot() {
        return Map.copyOf(MEMORY_STORAGE);
    }

    public static Set<String> keys() {
        return MEMORY_STORAGE.keySet();
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

    public static <T> List<T> getList(String key, TypeReference<T> typeReference) {
        List<T> list = new ArrayList<>();
        for (String k : keys()) {
            if (k.contains(key)) {
                list.add(get(k, typeReference));
            }
        }
        return list;
    }

    public static <T> T getListItem(String key, String itemIndex, TypeReference<T> typeReference) {
        String itemKey = key + "-" + itemIndex;
        for (String k : keys()) {
            if (k.equals(itemKey)) {
                return get(itemKey, typeReference);
            }
        }
        return null;
    }

    public static void put(String key, String value) {
        put(key, value, DEFAULT_SURVIVAL_SECOND);
    }


    public static <T> void put(String key, T value) {
        put(key, value, DEFAULT_SURVIVAL_SECOND);
    }

    public static <T> void put(String key, T value, long survivalSecond) {
        MEMORY_STORAGE.put(key, new MemoryItem()
                .setKey(key)
                .setValue(value instanceof String ? value.toString() : JSON.toJSONString(value, JSONWriter.Feature.WriteMapNullValue))
                .setSaveTimeStamp(System.currentTimeMillis())
                .setSurvivalSecond(survivalSecond));
    }

    public static void putForever(String key, String value) {
        put(key, value, FOREVER_SURVIVAL_SECOND);
    }

    public static <T> void putForever(String key, T value) {
        put(key, value, FOREVER_SURVIVAL_SECOND);
    }

    public static String putList(String key, String value) {
        return putList(key, value, DEFAULT_SURVIVAL_SECOND);
    }

    public static <T> String putList(String key, T value) {
        return putList(key, value, DEFAULT_SURVIVAL_SECOND);
    }

    public static <T> String putList(String key, T value, long survivalSecond) {
        String itemIndex = UuidUtil.genId();
        put(key + "-" + itemIndex, value, survivalSecond);
        return itemIndex;
    }

    public static String putListForever(String key, String value) {
        return putList(key, value, FOREVER_SURVIVAL_SECOND);
    }

    public static <T> String putListForever(String key, T value) {
        return putList(key, value, FOREVER_SURVIVAL_SECOND);
    }

    public static void remove(String key) {
        MEMORY_STORAGE.remove(key);
    }

    public static void removeListItem(String key, String itemIndex) {
        MEMORY_STORAGE.remove(key + "-" + itemIndex);
    }
}
