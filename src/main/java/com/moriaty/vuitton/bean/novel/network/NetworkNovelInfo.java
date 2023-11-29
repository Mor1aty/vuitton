package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:18
 */
@Data
@Accessors(chain = true)
public class NetworkNovelInfo {

    private String name;

    private String author;

    private String intro;

    private String imgUrl;

    private String chapterUrl;

    private String storageKey;

    private String downloaderMark;

}
