package com.moriaty.vuitton.bean.novel.network.api;

import lombok.Data;

/**
 * <p>
 * 获取目录 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 22:14
 */
@Data
public class FindNetworkNovelCatalogueReq {

    private String downloaderMark;

    private String chapterUrl;

}
