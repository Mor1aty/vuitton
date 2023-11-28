package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 网络历史信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午8:22
 */
@Data
@Accessors(chain = true)
public class NetworkHistoryInfo {

    private String tabIndex;

    private String downloaderMarkList;

    private String localSearchText;

    private String networkSearchText;

    private String queryStorageKey;

}
