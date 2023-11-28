package com.moriaty.vuitton.bean.novel.network;

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
public class FindNetworkCatalogueReq {

    private String downloaderMark;

    private String catalogueAppend;

}
