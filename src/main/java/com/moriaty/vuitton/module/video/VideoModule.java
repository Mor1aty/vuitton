package com.moriaty.vuitton.module.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.bean.video.VideoViewHistoryInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.dao.entity.VideoViewHistory;
import com.moriaty.vuitton.dao.mapper.VideoEpisodeMapper;
import com.moriaty.vuitton.dao.mapper.VideoMapper;
import com.moriaty.vuitton.dao.mapper.VideoViewHistoryMapper;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * 视频模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/10 22:36
 */
@Service("videoModule")
@RequiredArgsConstructor
@Slf4j
public class VideoModule implements InitializingBean {

    private final VideoMapper videoMapper;

    private final VideoEpisodeMapper videoEpisodeMapper;

    private final VideoViewHistoryMapper videoViewHistoryMapper;

    @Value("${file-server.video.location}")
    private String fsVideoLocation;

    @Value("${file-server.video.url-prefix}")
    private String fsVideoUrlPrefix;

    @Value("${file-server.video.module-video-img}")
    private String fsVideoModuleImg;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(0)
                .setName("视频")
                .setPath("video")
                .setImgUrl(fsVideoModuleImg));
    }

    public List<Video> findVideo(String id, String name) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<Video>().orderByAsc(Video::getId);
        if (StringUtils.hasText(id)) {
            queryWrapper.eq(Video::getId, id);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Video::getName, name);
        }
        return videoMapper.selectList(queryWrapper);
    }

    public List<VideoEpisode> findVideoEpisode(String videoId) {
        return videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .orderByAsc(VideoEpisode::getEpisodeIndex));
    }

    public VideoAroundEpisode findVideoAroundEpisode(String videoId, int episodeIndex) {
        LambdaQueryWrapper<VideoEpisode> queryWrapper = new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideo, videoId)
                .and(qw -> qw.eq(VideoEpisode::getEpisodeIndex, episodeIndex - 1)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeIndex)
                        .or().eq(VideoEpisode::getEpisodeIndex, episodeIndex + 1))
                .orderByAsc(VideoEpisode::getEpisodeIndex);
        List<VideoEpisode> episodeList = videoEpisodeMapper.selectList(queryWrapper);
        VideoAroundEpisode aroundEpisode = new VideoAroundEpisode();
        for (VideoEpisode episode : episodeList) {
            if (episode.getEpisodeIndex() == episodeIndex) {
                aroundEpisode.setEpisode(episode);
            }
            if (episode.getEpisodeIndex() == episodeIndex - 1) {
                aroundEpisode.setPreEpisode(episode);
            }
            if (episode.getEpisodeIndex() == episodeIndex + 1) {
                aroundEpisode.setNextEpisode(episode);
            }
        }
        return aroundEpisode;
    }

    public List<VideoViewHistoryInfo> findVideViewHistory(String videoId) {
        return videoViewHistoryMapper.findVideoViewHistory(videoId);
    }

    public void insertVideoViewHistory(String videoId, int episodeIndex) {
        videoViewHistoryMapper.delete(new LambdaQueryWrapper<VideoViewHistory>()
                .eq(VideoViewHistory::getVideo, videoId)
                .eq(VideoViewHistory::getEpisodeIndex, episodeIndex));
        videoViewHistoryMapper.insert(new VideoViewHistory()
                .setId(UuidUtil.genId())
                .setViewTime(LocalDateTime.now().format(Constant.Date.FORMAT_RECORD_TIME))
                .setVideo(videoId)
                .setEpisodeIndex(episodeIndex)
        );
    }

    public boolean enterVideo(String name, String coverImg, String description) {
        File baseFileFolder = new File(fsVideoLocation);
        File[] fileList = baseFileFolder.listFiles();
        if (fileList == null) {
            log.info("{} 目录为空", fsVideoLocation);
            return false;
        }
        for (File videoFile : fileList) {
            if (videoFile.getName().equals(name)) {
                Video video = new Video()
                        .setId(UuidUtil.genId())
                        .setName(videoFile.getName())
                        .setCoverImg(coverImg)
                        .setDescription(description);
                videoMapper.insert(video);
                enterVideoEpisode(video.getId(), videoFile);
            }
        }
        return true;
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
                        .setEpisodeUrl(fsVideoUrlPrefix + video.getName() + "/" + episodeFile.getName()));
                index++;
            }
        }
    }
}
