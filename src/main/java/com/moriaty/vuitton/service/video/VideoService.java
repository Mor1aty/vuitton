package com.moriaty.vuitton.service.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.bean.video.FindVideoReq;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.dao.mapper.VideoEpisodeMapper;
import com.moriaty.vuitton.dao.mapper.VideoMapper;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 视频 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 17:58
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Module(id = 1, name = "视频", path = "video")
public class VideoService {

    private final VideoMapper videoMapper;

    private final VideoEpisodeMapper videoEpisodeMapper;

    @Value("${video.base-path}")
    private String videoBasePath;

    @Value("${video.base-prefix}")
    private String videoBasePrefix;

    public Wrapper<List<Video>> findVideo(FindVideoReq req) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<Video>().orderByAsc(Video::getId);
        if (StringUtils.hasText(req.getId())) {
            queryWrapper.eq(Video::getId, req.getId());
        }
        if (StringUtils.hasText(req.getName())) {
            queryWrapper.like(Video::getName, req.getName());
        }
        List<Video> videoList = videoMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", videoList);
    }

    public Wrapper<List<VideoEpisode>> findVideoEpisode(String videoId) {
        List<VideoEpisode> episodeList = videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .orderByAsc(VideoEpisode::getEpisodeIndex));
        return WrapMapper.ok("获取成功", episodeList);
    }

    public Wrapper<VideoAroundEpisode> findVideoAroundEpisode(String videoId, int episodeId) {
        LambdaQueryWrapper<VideoEpisode> queryWrapper = new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .and(qw -> qw.eq(VideoEpisode::getEpisodeIndex, episodeId - 1)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeId)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeId + 1))
                .orderByAsc(VideoEpisode::getEpisodeIndex);
        List<VideoEpisode> episodeList = videoEpisodeMapper.selectList(queryWrapper);
        VideoAroundEpisode aroundEpisode = new VideoAroundEpisode();
        for (VideoEpisode episode : episodeList) {
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

    public Wrapper<Void> enterVideo(List<String> videoNameList) {
        File baseFileFolder = new File(videoBasePath);
        File[] fileList = baseFileFolder.listFiles();
        if (fileList == null) {
            log.info("{} 目录为空", videoBasePath);
            return WrapMapper.failure(videoBasePath + "目录为空");
        }
        for (File videoFile : fileList) {
            if (videoNameList.contains(videoFile.getName())) {
                Video video = new Video()
                        .setId(UuidUtil.genId())
                        .setName(videoFile.getName());
                videoMapper.insert(video);
                enterVideoEpisode(video.getId(), videoFile);
            }
        }
        return WrapMapper.ok("录入视频成功");
    }

    private void enterVideoEpisode(String videoId, File video) {
        File[] episodeFiles = video.listFiles();
        if (episodeFiles != null && episodeFiles.length > 0) {
            int index = 1;
            List<File> episodeFileList = Arrays.stream(episodeFiles)
                    .sorted(Comparator.comparing(File::getName)).toList();
            for (File episodeFile : episodeFileList) {
                // 仅支持 mp4
                if (!episodeFile.getName().endsWith(".mp4")) {
                    continue;
                }
                videoEpisodeMapper.insert(new VideoEpisode()
                        .setId(UuidUtil.genId())
                        .setVideo(videoId)
                        .setEpisodeIndex(index)
                        .setEpisodeName(episodeFile.getName()
                                .substring(0, episodeFile.getName().lastIndexOf(".")))
                        .setEpisodeUrl(videoBasePrefix + video.getName() + "/" + episodeFile.getName()));
                index++;
            }
        }
    }
}
