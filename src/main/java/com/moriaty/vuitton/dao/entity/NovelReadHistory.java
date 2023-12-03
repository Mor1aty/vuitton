package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说阅读历史
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 14:37
 */
@Data
@TableName("`novel_read_history`")
@Accessors(chain = true)
public class NovelReadHistory {

    @TableId(type = IdType.INPUT)
    private String id;

    private String readTime;

    private String novel;

    private Integer chapterIndex;
}
