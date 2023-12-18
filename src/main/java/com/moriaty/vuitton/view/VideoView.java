package com.moriaty.vuitton.view;

import com.moriaty.vuitton.ServerInfo;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.bean.video.VideoViewHistoryInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.module.video.VideoModule;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
public class VideoView {

    private final VideoModule videoModule;

    @RequestMapping
    @ViewLog
    public String video(Model model, @RequestParam(value = "searchText", required = false) String searchText) {
        List<Video> videoList = videoModule.findVideo(null, searchText);
        model.addAttribute("videoList", videoList);
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());
        model.addAttribute("searchText", searchText);
        if (!StringUtils.hasText(searchText)) {
            List<VideoViewHistoryInfo> viewHistory = videoModule.findVideViewHistory(null);
            model.addAttribute("viewHistory",
                    viewHistory.isEmpty() ? null : viewHistory.getFirst());
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
        List<Video> videoList = videoModule.findVideo(videoId, null);
        if (videoList.size() != 1) {
            return ViewUtil.goError(model, "视频信息出问题啦", KeyValuePair.of(
                    "videoList", String.valueOf(videoList)));
        }
        List<VideoEpisode> episodeList = videoModule.findVideoEpisode(videoId);
        if (episodeList.isEmpty()) {
            return ViewUtil.goError(model, "视频剧集出问题啦", KeyValuePair.ofList(
                    "videoList", String.valueOf(videoList),
                    "episodeList", String.valueOf(episodeList)));
        }
        model.addAttribute("videoInfo", videoList.getFirst());
        model.addAttribute("episodeList", episodeList);
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());

        List<VideoViewHistoryInfo> viewHistoryList = videoModule.findVideViewHistory(videoId);
        model.addAttribute("viewHistory", viewHistoryList.isEmpty() ? null : viewHistoryList.getFirst());
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
        VideoAroundEpisode aroundEpisode = videoModule.findVideoAroundEpisode(videoId, episodeIndex);
        if (aroundEpisode == null) {
            return ViewUtil.goError(model, "视频播放出问题啦", KeyValuePair.ofList(
                    "videoId", videoId, "episodeIndex", episodeIndexStr));
        }
        videoModule.insertVideoViewHistory(videoId, episodeIndex);
        model.addAttribute("aroundEpisode", aroundEpisode);
        model.addAttribute("fileServerUrl", ServerInfo.BASE_INFO.getFileServerUrl());
        return "video/video_play";
    }

    @RequestMapping("video_view_history")
    @ViewLog
    public String videoViewHistory(Model model,
                                   @RequestParam(value = "videoId", required = false) String videoId,
                                   @RequestParam(value = "searchText", required = false) String searchText) {
        List<VideoViewHistoryInfo> viewHistoryList = videoModule.findVideViewHistory(videoId);
        model.addAttribute("viewHistoryList", viewHistoryList);
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
        boolean isEnterVideo = videoModule.enterVideo(name, coverImg, description);
        model.addAttribute("actionMsg", isEnterVideo ? "新增视频成功" : "新增视频失败");
        model.addAttribute("backUrl", "/video");
        return "action_result";
    }
}
