package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.video.FindVideoReq;
import com.moriaty.vuitton.bean.video.VideoAroundEpisode;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.dao.entity.VideoEpisode;
import com.moriaty.vuitton.service.video.VideoService;
import com.moriaty.vuitton.service.view.ViewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;


/**
 * <p>
 * 视图 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:30
 */
@Controller
@AllArgsConstructor
@Slf4j
public class ViewCtrl {

    private final ViewService viewService;

    private final VideoService videoService;

    @RequestMapping({"/", "/index"})
    public String index(final Model model) {
        model.addAttribute("moduleList", viewService.findAllModule());
        return "index";
    }

    @RequestMapping("/novel")
    public String novel(Model model) {
        return "novel";
    }

    @RequestMapping("/video")
    public String video(Model model) {
        Wrapper<List<Video>> videoWrapper = videoService.findVideo(new FindVideoReq());
        model.addAttribute("videoList",
                WrapMapper.isOk(videoWrapper) ? videoWrapper.data() : Collections.emptyList());
        return "video";
    }

    @RequestMapping("/video_info")
    public String videoInfo(Model model, @RequestParam("videoId") String videoIdStr) {
        if (!videoIdStr.matches("^\\d*[1-9]\\d*$")) {
            return goError(model, "视频 id 出问题啦", "videoId=" + videoIdStr);
        }
        int videoId = Integer.parseInt(videoIdStr);
        Wrapper<List<Video>> videoWrapper = videoService.findVideo(new FindVideoReq().setId(videoId));
        if (!WrapMapper.isOk(videoWrapper) || videoWrapper.data().size() != 1) {
            return goError(model, "视频信息出问题啦", "videoWrapper=" + videoWrapper);
        }
        Wrapper<List<VideoEpisode>> episodeMapper = videoService.findVideoEpisode(videoId);
        if (!WrapMapper.isOk(episodeMapper)) {
            return goError(model, "视频剧集出问题啦", "videoWrapper=" + videoWrapper
                    + " episodeMapper=" + episodeMapper);
        }
        model.addAttribute("videoInfo", videoWrapper.data().get(0));
        model.addAttribute("episodeList", episodeMapper.data());
        return "video_info";
    }

    @RequestMapping("/video_play")
    public String videoPlay(Model model, @RequestParam("videoId") String videoIdStr,
                            @RequestParam("episodeId") String episodeIdStr) {
        if (!videoIdStr.matches("^\\d*[1-9]\\d*$") || !episodeIdStr.matches("^\\d*[1-9]\\d*$")) {
            return goError(model, "视频参数出问题啦", "videoId=" + videoIdStr
                    + " episodeId=" + episodeIdStr);
        }
        int videoId = Integer.parseInt(videoIdStr);
        int episodeId = Integer.parseInt(episodeIdStr);
        Wrapper<VideoAroundEpisode> aroundEpisodeWrapper = videoService.findVideoAroundEpisode(videoId, episodeId);
        if (!WrapMapper.isOk(aroundEpisodeWrapper)) {
            return goError(model, "视频播放出问题啦", "videoId=" + videoIdStr
                    + " episodeId=" + episodeIdStr + " aroundEpisodeWrapper=" + aroundEpisodeWrapper);
        }
        log.info("{}", aroundEpisodeWrapper.data());
        model.addAttribute("aroundEpisode", aroundEpisodeWrapper.data());
        return "video_play";
    }

    private String goError(Model model, String errorMsg, String data) {
        model.addAttribute("errorMsg", errorMsg);
        model.addAttribute("data", data);
        return "error";
    }
}
