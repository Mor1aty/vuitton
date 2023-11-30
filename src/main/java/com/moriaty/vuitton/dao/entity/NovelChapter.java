package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说章节
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 下午2:02
 */
@Data
@TableName("`novel_chapter`")
@Accessors(chain = true)
public class NovelChapter {

    @TableId(type = IdType.INPUT)
    private String id;

    private String novel;

    private Integer chapterIndex;

    private String chapterTitle;

    private String content;

    private String contentHtml;

}
