package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
public class Video {

    private Integer id;

    private String name;

    private String description;

    private String coverImg;

}
