package com.moriaty.vuitton.util;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * <p>
 * View 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午3:42
 */
public class ViewUtil {

    private ViewUtil() {

    }

    public static String goError(Model model, String errorMsg, String data) {
        model.addAttribute("errorMsg", errorMsg);
        model.addAttribute("data", data);
        return "error";
    }

    public static String goError(Model model, String errorMsg, KeyValuePair... keyValuePairs) {
        StringBuilder sb = new StringBuilder();
        for (KeyValuePair keyValuePair : keyValuePairs) {
            sb.append(keyValuePair.getKey()).append(": ").append(keyValuePair.getValue()).append(", ");
        }
        return goError(model, errorMsg, sb.substring(0, sb.length() - 2));
    }

    public static String goParamError(Model model, KeyValuePair... keyValuePairs) {
        return goError(model, "参数出问题啦", keyValuePairs);
    }

    public static String goParamError(Model model, List<KeyValuePair> keyValuePairs) {
        return goError(model, "参数出问题啦", keyValuePairs);
    }

    public static String goError(Model model, String errorMsg, List<KeyValuePair> keyValuePairs) {
        StringBuilder sb = new StringBuilder();
        for (KeyValuePair keyValuePair : keyValuePairs) {
            sb.append(keyValuePair.getKey()).append(": ").append(keyValuePair.getValue()).append(", ");
        }
        return goError(model, errorMsg, sb.substring(0, sb.length() - 2));
    }

    public static boolean checkIllegalParam(List<String> params) {
        for (String param : params) {
            if (!StringUtils.hasText(param)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIllegalParam(String... params) {
        return checkIllegalParam(List.of(params));
    }

    public static boolean checkIllegalParam(List<String> params, BooleanSupplier... suppliers) {
        if (checkIllegalParam(params)) {
            return true;
        }
        for (BooleanSupplier supplier : suppliers) {
            if (supplier.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkLoaded(Model model, String loadedKey) {
        if (StringUtils.hasText(loadedKey)) {
            Map<String, Object> modelAttrMap = MemoryStorage.get(loadedKey, new TypeReference<>() {
            });
            if (modelAttrMap != null) {
                model.addAllAttributes(modelAttrMap);
                return true;
            }
        }
        return false;
    }
}
