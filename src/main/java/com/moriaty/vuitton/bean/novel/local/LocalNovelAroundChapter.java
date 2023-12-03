package com.moriaty.vuitton.bean.novel.local;

import com.moriaty.vuitton.dao.entity.NovelChapter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 本地小说前后章节
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 14:19
 */
@Data
@Accessors(chain = true)
public class LocalNovelAroundChapter {

    private NovelChapter chapter;

    private NovelChapter preChapter;

    private NovelChapter nextChapter;
}
