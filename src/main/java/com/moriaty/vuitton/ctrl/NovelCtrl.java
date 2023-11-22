package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.novel.*;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.novel.NovelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:10
 */
@RestController
@RequestMapping("novel")
@AllArgsConstructor
@Slf4j
public class NovelCtrl {

    private final NovelService novelService;

    @PostMapping("queryNovel")
    public Wrapper<List<QueryNovelInfo>> queryNovel(@RequestBody QueryNovelReq req) {
        log.info("novel/queryNovel, req: {}", req);
        return novelService.queryNovel(req);
    }

    @PostMapping("downloadNovel")
    public Wrapper<String> downloadNovel(@RequestBody DownloadNovelReq req) {
        log.info("novel/downloadNovel, req: {}", req);
        return novelService.downloadNovel(req);
    }

    @PostMapping("findCatalogue")
    public Wrapper<List<NovelChapter>> findCatalogue(@RequestBody FindCatalogueReq req) {
        log.info("novel/findCatalogue, req: {}", req);
        return novelService.findCatalogue(req);
    }

    @PostMapping("findContent")
    public Wrapper<NovelContent> findContent(@RequestBody FindContentReq req) {
        log.info("novel/findContent, req: {}", req);
        return novelService.findContent(req);
    }
}
