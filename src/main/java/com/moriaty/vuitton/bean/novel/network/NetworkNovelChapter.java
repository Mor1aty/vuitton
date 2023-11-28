package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说章节
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 21:21
 */
@Data
@Accessors(chain = true)
public class NetworkNovelChapter {

    private int index;

    private String name;

    private String url;
}
