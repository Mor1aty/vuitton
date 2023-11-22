package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.video.VideoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
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
@RequestMapping("test")
@AllArgsConstructor
@Slf4j
public class TestCtrl {

    private final VideoService videoService;

    @GetMapping("enterVideo")
    public Wrapper<Void> enterVideo() {
        List<String> videoNameList = Arrays.asList("2012 Legal High 第一季+SP1",
                "2013 Legal High 第二季+SP2",
                "吹响吧 上低音号2",
                "叛逆的鲁鲁修1",
                "叛逆的鲁鲁修2",
                "妻子变成小学生",
                "死亡笔记",
                "甄嬛传",
                "Unnatural.2018",
                "Unpretty Rapstar 3");
        return videoService.enterVideo(videoNameList);
    }
}
