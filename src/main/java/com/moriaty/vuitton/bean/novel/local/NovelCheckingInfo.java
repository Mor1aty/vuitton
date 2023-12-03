package com.moriaty.vuitton.bean.novel.local;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说检查中信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 16:09
 */
@Data
@Accessors(chain = true)
public class NovelCheckingInfo {

    private Integer chapterIndex;

}
