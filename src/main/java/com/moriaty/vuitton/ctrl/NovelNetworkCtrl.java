package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.novel.network.*;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.novel.NovelNetworkService;
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
@RequestMapping("api/novel/network")
@AllArgsConstructor
@Slf4j
public class NovelNetworkCtrl {

    private final NovelNetworkService novelNetworkService;

    @PostMapping("queryNovel")
    public Wrapper<List<QueryNetworkNovelInfo>> queryNovel(@RequestBody QueryNetworkNovelReq req) {
        log.info("novel/queryNovel, req: {}", req);
        return novelNetworkService.queryNovel(req.getSearchText(), req.getDownloaderMarkList());
    }

    @PostMapping("downloadNetworkNovel")
    public Wrapper<String> downloadNetworkNovel(@RequestBody DownloadNetworkNovelReq req) {
        log.info("novel/downloadNetworkNovel, req: {}", req);
        return novelNetworkService.downloadNovel(req);
    }

    @PostMapping("findNetworkCatalogue")
    public Wrapper<List<NetworkNovelChapter>> findNetworkCatalogue(@RequestBody FindNetworkCatalogueReq req) {
        log.info("novel/findCatalogue, req: {}", req);
        return novelNetworkService.findCatalogue(req);
    }

    @PostMapping("findContent")
    public Wrapper<NetworkNovelContent> findContent(@RequestBody FindNetworkContentReq req) {
        log.info("novel/findContent, req: {}", req);
        return novelNetworkService.findContent(req);
    }
}
