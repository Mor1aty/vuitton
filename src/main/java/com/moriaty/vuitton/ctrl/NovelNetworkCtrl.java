package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.service.novel.NovelNetworkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:10
 */
@RestController
@RequestMapping("api/novel/network")
@AllArgsConstructor
@Slf4j
public class NovelNetworkCtrl {

    private final NovelNetworkService novelNetworkService;

}
