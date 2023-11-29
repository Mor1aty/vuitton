package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;

/**
 * <p>
 * 获取内容 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 22:19
 */
@Data
public class FindNetworkNovelContentReq {

    private String chapterName;

    private String chapterUrl;

    private String downloaderMark;

}
