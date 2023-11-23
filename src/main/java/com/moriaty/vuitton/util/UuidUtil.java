package com.moriaty.vuitton.util;

import com.moriaty.vuitton.constant.Constant;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * UUID 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/22 下午9:26
 */
public class UuidUtil {

    private UuidUtil() {

    }

    public static String genUuid4() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 4);
    }

    public static String genId() {
        return LocalDateTime.now().format(Constant.Date.FORMAT_ID) + genUuid4();
    }
}
