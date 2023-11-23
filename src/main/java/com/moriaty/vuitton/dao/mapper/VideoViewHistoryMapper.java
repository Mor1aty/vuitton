package com.moriaty.vuitton.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moriaty.vuitton.bean.video.VideoViewHistoryInfo;
import com.moriaty.vuitton.dao.entity.VideoViewHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 视频观看历史 Mapper
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/23 下午1:23
 */
public interface VideoViewHistoryMapper extends BaseMapper<VideoViewHistory> {

    /**
     * 查询视频观看记录详情
     *
     * @param videoId String
     * @return List with VideoViewHistoryInfo
     */
    @Select("""
            <script>
            SELECT
            vvh.id AS viewId,
            vvh.view_time AS viewTime,
            vvh.video AS videoId,
            v.`name` AS videoName,
            v.cover_img AS videoCoverImg,
            vvh.episode_index AS episodeIndex,
            ve.episode_name AS episodeName,
            ve.episode_url AS episodeUrl
            FROM video_view_history vvh
            LEFT JOIN video v ON vvh.video = v.id
            LEFT JOIN video_episode ve ON vvh.video = ve.video AND vvh.episode_index = ve.episode_index
            WHERE 1 = 1
            <if test="videoId != null and videoId != ''"> AND vvh.video = #{videoId} </if>
            ORDER BY vvh.view_time DESC
            </script>""")
    List<VideoViewHistoryInfo> findVideoViewHistory(@Param("videoId") String videoId);
}
