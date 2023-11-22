package com.moriaty.vuitton.service.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.ServerInfo;
import com.moriaty.vuitton.bean.video.FindVideoReq;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.dao.mapper.VideoEpisodeMapper;
import com.moriaty.vuitton.dao.mapper.VideoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 视频 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 17:58
 */
@Service
@AllArgsConstructor
@Slf4j
@Module(id = 1, name = "视频", path = "video")
public class VideoService {

    private final VideoMapper videoMapper;

    private final VideoEpisodeMapper videoEpisodeMapper;

    public Wrapper<List<Video>> findVideo(FindVideoReq req) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<Video>().orderByDesc(Video::getId);
        if (req.getId() != null && req.getId() >= 0) {
            queryWrapper.eq(Video::getId, req.getId());
        }
        if (StringUtils.hasText(req.getName())) {
            queryWrapper.like(Video::getName, req.getName());
        }
        List<Video> videoList = videoMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", videoList);
    }

    public Wrapper<List<VideoEpisode>> findVideoEpisode(int videoId) {
        List<VideoEpisode> episodeList = videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .orderByAsc(VideoEpisode::getEpisodeIndex));
        return WrapMapper.ok("获取成功", episodeList);
    }

    public Wrapper<VideoAroundEpisode> findVideoAroundEpisode(int videoId, int episodeId) {
        LambdaQueryWrapper<VideoEpisode> queryWrapper = new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .and(qw -> qw.eq(VideoEpisode::getEpisodeIndex, episodeId - 1)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeId)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeId + 1))
                .orderByAsc(VideoEpisode::getEpisodeIndex);
        List<VideoEpisode> episodeList = videoEpisodeMapper.selectList(queryWrapper);
        VideoAroundEpisode aroundEpisode = new VideoAroundEpisode();
        for (VideoEpisode episode : episodeList) {
            episode.setEpisodeUrl(ServerInfo.BASE_INFO.getFileServerUrl() + episode.getEpisodeUrl());
            if (episode.getEpisodeIndex() == episodeId) {
                aroundEpisode.setEpisode(episode);
            }
            if (episode.getEpisodeIndex() == episodeId - 1) {
                aroundEpisode.setPreEpisode(episode);
            }
            if (episode.getEpisodeIndex() == episodeId + 1) {
                aroundEpisode.setNextEpisode(episode);
            }
        }
        return WrapMapper.ok("获取成功", aroundEpisode);
    }
}
