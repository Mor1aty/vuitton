package com.moriaty.vuitton.bean.video;

import com.moriaty.vuitton.dao.entity.VideoEpisode;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频前后剧集
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 3:01
 */
@Data
@Accessors(chain = true)
public class VideoAroundEpisode {

    private VideoEpisode episode;

    private VideoEpisode preEpisode;

    private VideoEpisode nextEpisode;

}
