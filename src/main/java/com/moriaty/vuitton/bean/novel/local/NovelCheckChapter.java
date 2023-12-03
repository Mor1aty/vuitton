package com.moriaty.vuitton.bean.novel.local;

import com.moriaty.vuitton.dao.entity.NovelChapter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说校验章节
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 15:14
 */
@Data
@Accessors(chain = true)
public class NovelCheckChapter {

    private NovelChapter preNearChapter;

    private NovelChapter nextNearChapter;

    private Integer chapterIndex;
}
