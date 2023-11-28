package com.moriaty.vuitton.core.storage;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 内存 Item
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午5:05
 */
@Data
@Accessors(chain = true)
public class MemoryItem {

    /**
     * 存活秒数
     */
    private long survivalSecond;

    /**
     * 存入时间戳
     */
    private long saveTimeStamp;

    private String key;

    private String value;

}
