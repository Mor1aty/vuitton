package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 下午2:00
 */
@Data
@TableName("`novel`")
@Accessors(chain = true)
public class Novel {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String author;

    private String intro;

    private String imgUrl;

    private String filePath;

    private String downloaderMark;

}
