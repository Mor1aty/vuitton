package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("hello")
    public Wrapper<String> hello() {
        return WrapMapper.ok("hello", "world");
    }
}
