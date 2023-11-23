package com.moriaty.vuitton.bean.video;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频观看历史信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/23 下午2:21
 */
@Data
@Accessors(chain = true)
public class VideoViewHistoryInfo {

    private String viewId;

    private String viewTime;

    private String videoId;

    private String videoName;

    private String videoCoverImg;

    private Integer episodeIndex;

    private String episodeName;

    private String episodeUrl;

}
