package com.moriaty.vuitton.bean.novel.network.api;

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

    private String novelName;

    private String novelAuthor;

    private String novelIntro;

    private String novelImgUrl;

    private String novelChapterUrl;

}
