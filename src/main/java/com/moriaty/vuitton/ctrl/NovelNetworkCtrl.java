package com.moriaty.vuitton.ctrl;

import com.moriaty.vuitton.bean.novel.network.*;
import com.moriaty.vuitton.bean.novel.network.api.DownloadNetworkNovelReq;
import com.moriaty.vuitton.bean.novel.network.api.FindNetworkNovelCatalogueReq;
import com.moriaty.vuitton.bean.novel.network.api.FindNetworkNovelContentReq;
import com.moriaty.vuitton.bean.novel.network.api.QueryNetworkNovelReq;
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
        return novelNetworkService.queryNovel(req.getSearchText(), req.getDownloaderMarkList());
    }

    @PostMapping("downloadNetworkNovel")
    public Wrapper<String> downloadNetworkNovel(@RequestBody DownloadNetworkNovelReq req) {
        log.info("novel/downloadNetworkNovel, req: {}", req);
        return novelNetworkService.downloadNovel(req.getDownloaderMark(), new NetworkNovelInfo()
                .setName(req.getNovelName())
                .setAuthor(req.getNovelAuthor())
                .setIntro(req.getNovelIntro())
                .setImgUrl(req.getNovelImgUrl())
                .setCatalogueUrl(req.getNovelChapterUrl()));
    }

    @PostMapping("findNetworkNovelCatalogue")
    public Wrapper<List<NetworkNovelChapter>> findNetworkNovelCatalogue(@RequestBody FindNetworkNovelCatalogueReq req) {
        return novelNetworkService.findCatalogue(req.getDownloaderMark(), req.getCatalogueUrl());
    }

    @PostMapping("findNetworkNovelContent")
    public Wrapper<NetworkNovelContent> findNetworkNovelContent(@RequestBody FindNetworkNovelContentReq req) {
        return novelNetworkService.findContent(req.getChapterName(), req.getChapterUrl(), req.getDownloaderMark());
    }
}
