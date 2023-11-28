package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 查询小说信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:28
 */
@Data
@Accessors(chain = true)
public class QueryNetworkNovelInfo {

    private String webSearch;

    private String errorMsg;

    private String sourceWebsite;

    private String sourceName;

    private String sourceMark;

    private List<NetworkNovelInfo> novelInfoList;

}
