package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.novel.NovelNetworkService;
import com.moriaty.vuitton.service.video.VideoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 测试 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/22 下午8:04
 */
@RestController
@RequestMapping("api/test")
@AllArgsConstructor
@Slf4j
public class TestCtrl {

    private final NovelNetworkService novelNetworkService;

    private final VideoService videoService;

    @GetMapping("hello")
    public Wrapper<String> hello() {
        return WrapMapper.ok("hello", "world");
    }

    @GetMapping("enterVideo")
    public Wrapper<Void> enterVideo() {
        return videoService.enterVideo("大明王朝1566", null, null);
    }

    @GetMapping("queryNovel")
    public Wrapper<Void> queryNovel() {
        Wrapper<List<QueryNetworkNovelInfo>> result = novelNetworkService.queryNovel("修罗武神", null);
        for (QueryNetworkNovelInfo queryNovelInfo : result.data()) {
            if (queryNovelInfo.getNovelInfoList() == null) {
                continue;
            }
            for (NetworkNovelInfo novelInfo : queryNovelInfo.getNovelInfoList()) {
                log.info("{}", novelInfo);
            }
        }
        return WrapMapper.ok("success");
    }
}
