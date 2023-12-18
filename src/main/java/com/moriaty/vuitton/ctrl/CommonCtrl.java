package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.common.CommonInfo;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.CommonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 通用 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/8 1:20
 */
@RestController
@RequestMapping("api/common")
@AllArgsConstructor
@Slf4j
public class CommonCtrl {

    private final CommonService commonService;

    @GetMapping("info")
    public Wrapper<CommonInfo> info() {
        return commonService.info();
    }
}
