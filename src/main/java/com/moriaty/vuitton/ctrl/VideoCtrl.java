package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.video.api.FindVideoReq;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.service.video.VideoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 视频 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/10 22:26
 */
@RestController
@RequestMapping("api/video")
@AllArgsConstructor
@Slf4j
public class VideoCtrl {

    private final VideoService videoService;

    @PostMapping("findVideo")
    public Wrapper<List<Video>> findVideo(@RequestBody @Validated FindVideoReq req) {
        return videoService.findVideo(req);
    }
}
