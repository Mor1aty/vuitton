package com.moriaty.vuitton.bean.novel.network;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 网络小说前后章节
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/29 下午11:03
 */
@Data
@Accessors(chain = true)
public class NetworkNovelAroundChapter {

    private NetworkNovelChapter chapter;

    private NetworkNovelChapter preChapter;

    private NetworkNovelChapter nextChapter;
}
