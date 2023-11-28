package com.moriaty.vuitton.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * Thymeleaf 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 上午4:33
 */
public class ThymeleafUtil {

    public String objectToJson(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue);
    }

    public boolean stringHasText(String s) {
        return StringUtils.hasText(s);
    }

    public <T> boolean listHasValue(List<T> list) {
        return list != null && !list.isEmpty();
    }
}
