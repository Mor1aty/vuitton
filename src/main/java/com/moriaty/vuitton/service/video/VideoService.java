package com.moriaty.vuitton.service.video;

import com.moriaty.vuitton.bean.video.api.FindVideoReq;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Video;
import com.moriaty.vuitton.module.video.VideoModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class VideoService {

    private final VideoModule videoModule;

    public Wrapper<List<Video>> findVideo(FindVideoReq req) {
        List<Video> videoList = videoModule.findVideo(req.getId(), req.getName());
        return WrapMapper.ok(videoList);
    }
}
