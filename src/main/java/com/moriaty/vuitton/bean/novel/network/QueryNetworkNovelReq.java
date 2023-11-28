package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 查询小说 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:24
 */
@Data
@Accessors(chain = true)
public class QueryNetworkNovelReq {

    private String searchText;

    private List<String> downloaderMarkList;

}
