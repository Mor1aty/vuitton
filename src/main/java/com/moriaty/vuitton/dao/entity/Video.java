package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 22:32
 */
@Data
@TableName("`video`")
@Accessors(chain = true)
public class Video {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String description;

    private String coverImg;

}
