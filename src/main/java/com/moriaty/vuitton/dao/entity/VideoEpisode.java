package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
public class VideoEpisode {

    private Integer id;

    private Integer video;

    private Integer episodeIndex;

    private String episodeName;

    private String episodeUrl;

}
