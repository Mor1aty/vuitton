package com.moriaty.vuitton.view;

import com.moriaty.vuitton.ServerInfo;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.bean.video.VideoViewHistoryInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.service.video.VideoService;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;


/**
 * <p>
 * 视频 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:30
 */
@Controller
@RequestMapping("video")
@AllArgsConstructor
@Slf4j
public class VideoView implements InitializingBean {

    private final VideoService videoService;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(0)
                .setName("视频")
                .setPath("video"));
    }

    @RequestMapping
    @ViewLog
    public String video(Model model, @RequestParam(value = "searchText", required = false) String searchText) {
        Wrapper<List<Video>> videoWrapper = videoService.findVideo(null, searchText);
        model.addAttribute("videoList",
                WrapMapper.isOk(videoWrapper) ? videoWrapper.data() : Collections.emptyList());
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());
        model.addAttribute("searchText", searchText);
        if (!StringUtils.hasText(searchText)) {
            Wrapper<List<VideoViewHistoryInfo>> viewHistoryWrapper = videoService.findVideViewHistory(null);
            model.addAttribute("viewHistory",
                    !WrapMapper.isOk(viewHistoryWrapper) || viewHistoryWrapper.data().isEmpty() ?
                            null : viewHistoryWrapper.data().get(0));
        }
        return "video/video";
    }

    @RequestMapping("video_info")
    @ViewLog
    public String videoInfo(Model model, @RequestParam("videoId") String videoId,
                            @RequestParam(value = "searchText", required = false) String searchText) {
        if (ViewUtil.checkIllegalParam(videoId)) {
            return ViewUtil.goParamError(model, KeyValuePair.of("videoId", videoId));
        }
        Wrapper<List<Video>> videoWrapper = videoService.findVideo(videoId, null);
        if (!WrapMapper.isOk(videoWrapper) || videoWrapper.data().size() != 1) {
            return ViewUtil.goError(model, "视频信息出问题啦", KeyValuePair.of(
                    "videoWrapper", String.valueOf(videoWrapper)));
        }
        Wrapper<List<VideoEpisode>> episodeMapper = videoService.findVideoEpisode(videoId);
        if (!WrapMapper.isOk(episodeMapper)) {
            return ViewUtil.goError(model, "视频剧集出问题啦", KeyValuePair.ofList(
                    "videoWrapper", String.valueOf(videoWrapper),
                    "episodeMapper", String.valueOf(episodeMapper)));
        }
        model.addAttribute("videoInfo", videoWrapper.data().get(0));
        model.addAttribute("episodeList", episodeMapper.data());
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());

        Wrapper<List<VideoViewHistoryInfo>> viewHistoryWrapper = videoService.findVideViewHistory(videoId);
        model.addAttribute("viewHistory",
                !WrapMapper.isOk(viewHistoryWrapper) || viewHistoryWrapper.data().isEmpty() ?
                        null : viewHistoryWrapper.data().get(0));
        model.addAttribute("searchText", searchText);
        return "video/video_info";
    }

    @RequestMapping("video_play")
    @ViewLog
    public String videoPlay(Model model, @RequestParam("videoId") String videoId,
                            @RequestParam("episodeIndex") String episodeIndexStr) {
        if (ViewUtil.checkIllegalParam(List.of(videoId, episodeIndexStr),
                () -> !episodeIndexStr.matches(Constant.Regex.POSITIVE_INTEGER))) {
            return ViewUtil.goParamError(model, KeyValuePair.ofList(
                    "videoId", videoId, "episodeIndex", episodeIndexStr));
        }
        int episodeIndex = Integer.parseInt(episodeIndexStr);
        Wrapper<VideoAroundEpisode> aroundEpisodeWrapper = videoService.findVideoAroundEpisode(videoId, episodeIndex);
        if (!WrapMapper.isOk(aroundEpisodeWrapper)) {
            return ViewUtil.goError(model, "视频播放出问题啦", KeyValuePair.ofList(
                    "videoId", videoId, "episodeIndex", episodeIndexStr,
                    "aroundEpisodeWrapper", String.valueOf(aroundEpisodeWrapper)));
        }
        Wrapper<Void> insertWrapper = videoService.insertVideoViewHistory(videoId, episodeIndex);
        if (!WrapMapper.isOk(insertWrapper)) {
            return ViewUtil.goError(model, "插入观看记录出问题啦", KeyValuePair.ofList(
                    "videoId", videoId, "episodeIndex", episodeIndexStr));
        }
        model.addAttribute("aroundEpisode", aroundEpisodeWrapper.data());
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());
        return "video/video_play";
    }

    @RequestMapping("video_view_history")
    @ViewLog
    public String videoViewHistory(Model model,
                                   @RequestParam(value = "videoId", required = false) String videoId,
                                   @RequestParam(value = "searchText", required = false) String searchText) {
        Wrapper<List<VideoViewHistoryInfo>> historyWrapper = videoService.findVideViewHistory(videoId);
        if (!WrapMapper.isOk(historyWrapper)) {
            return ViewUtil.goError(model, "观看记录出问题啦",
                    KeyValuePair.of("historyWrapper", String.valueOf(historyWrapper)));
        }
        model.addAttribute("viewHistoryList", historyWrapper.data());
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());
        model.addAttribute("searchText", searchText);
        return "video/video_view_history";
    }

    @RequestMapping("video_add")
    @ViewLog
    public String videoAdd() {
        return "video/video_add";
    }

    @RequestMapping("action_add_video")
    @ViewLog
    public String actionAddVideo(Model model, @RequestParam("name") String name,
                                 @RequestParam(value = "coverImg", required = false) String coverImg,
                                 @RequestParam(value = "description", required = false) String description) {
        if (ViewUtil.checkIllegalParam(name)) {
            return ViewUtil.goParamError(model, KeyValuePair.of("name", name));
        }
        Wrapper<Void> enterVideoWrapper = videoService.enterVideo(name, coverImg, description);
        model.addAttribute("actionMsg", WrapMapper.isOk(enterVideoWrapper) ? "新增视频成功" : "新增视频失败");
        model.addAttribute("backUrl", "/video");
        return "action_result";
    }
}
