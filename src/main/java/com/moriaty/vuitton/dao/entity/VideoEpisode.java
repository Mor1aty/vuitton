package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频剧集
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 1:17
 */
@Data
@TableName("`video_episode`")
@Accessors(chain = true)
public class VideoEpisode {

    @TableId(type = IdType.INPUT)
    private String id;

    private String video;

    private Integer episodeIndex;

    private String episodeName;

    private String episodeUrl;

}
