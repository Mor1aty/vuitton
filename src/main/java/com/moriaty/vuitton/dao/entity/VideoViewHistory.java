package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频观看历史
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/23 下午1:21
 */
@Data
@TableName("`video_view_history`")
@Accessors(chain = true)
public class VideoViewHistory {

    @TableId(type = IdType.INPUT)
    private String id;

    private String viewTime;

    private String video;

    private Integer episodeIndex;

}
