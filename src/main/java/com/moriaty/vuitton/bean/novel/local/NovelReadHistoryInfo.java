package com.moriaty.vuitton.bean.novel.local;

import lombok.Data;

/**
 * <p>
 * 小说阅读历史信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 14:44
 */
@Data
public class NovelReadHistoryInfo {

    private String readId;

    private String readTime;

    private String novelId;

    private Integer chapterIndex;

    private String chapterName;
}
