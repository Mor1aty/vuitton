package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;

/**
 * <p>
 * 下载小说 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 18:16
 */
@Data
public class DownloadNetworkNovelReq {

    private String downloaderMark;

    private String catalogueAppend;

    private String novelName;

}
