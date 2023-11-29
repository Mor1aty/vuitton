package com.moriaty.vuitton.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 键值对
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 上午12:21
 */
@Data
@AllArgsConstructor(staticName = "of")
public class KeyValuePair {

    private String key;

    private String value;

    public static List<KeyValuePair> ofList(String k1, String v1) {
        return List.of(KeyValuePair.of(k1, v1));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4, String k5, String v5) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4), KeyValuePair.of(k5, v5));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4, String k5, String v5, String k6, String v6) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4), KeyValuePair.of(k5, v5), KeyValuePair.of(k6, v6));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4, String k5, String v5, String k6, String v6,
                                            String k7, String v7) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4), KeyValuePair.of(k5, v5), KeyValuePair.of(k6, v6),
                KeyValuePair.of(k7, v7));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4, String k5, String v5, String k6, String v6,
                                            String k7, String v7, String k8, String v8) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4), KeyValuePair.of(k5, v5), KeyValuePair.of(k6, v6),
                KeyValuePair.of(k7, v7), KeyValuePair.of(k8, v8));
    }

    public static List<KeyValuePair> ofList(String k1, String v1, String k2, String v2, String k3, String v3,
                                            String k4, String v4, String k5, String v5, String k6, String v6,
                                            String k7, String v7, String k8, String v8, String k9, String v9) {
        return List.of(KeyValuePair.of(k1, v1), KeyValuePair.of(k2, v2), KeyValuePair.of(k3, v3),
                KeyValuePair.of(k4, v4), KeyValuePair.of(k5, v5), KeyValuePair.of(k6, v6),
                KeyValuePair.of(k7, v7), KeyValuePair.of(k8, v8), KeyValuePair.of(k9, v9));
    }
}
