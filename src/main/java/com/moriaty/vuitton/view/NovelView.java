package com.moriaty.vuitton.view;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.novel.network.*;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.service.novel.NovelFactory;
import com.moriaty.vuitton.service.novel.NovelLocalService;
import com.moriaty.vuitton.service.novel.NovelNetworkService;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.util.UuidUtil;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <p>
 * 小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午3:43
 */
@Controller
@RequestMapping("novel")
@AllArgsConstructor
@Slf4j
public class NovelView implements InitializingBean {

    private final NovelNetworkService novelNetworkService;

    private final NovelLocalService novelLocalService;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(0)
                .setName("小说")
                .setPath("novel"));
    }

    @RequestMapping
    @ViewLog
    public String novel(Model model,
                        @RequestParam(value = "localSearchText", required = false) String localSearchText,
                        @RequestParam(value = "networkSearchText", required = false) String networkSearchText,
                        @RequestParam(value = "downloaderMark", required = false) List<String> downloaderMarkList,
                        @RequestParam(value = "tabIndex", required = false) String tabIndex,
                        @RequestParam(value = "novelPageKey", required = false) String novelPageKey) {
        if (ViewUtil.checkLoaded(model, novelPageKey)) {
            return "novel/novel";
        }
        model.addAttribute("downloaderList", NovelFactory.getAllDownloaderInfo());
        if (StringUtils.hasText(localSearchText)) {
            model = handleLocalNovel(model, localSearchText);
        }
        if (StringUtils.hasText(networkSearchText)) {
            model = handleNetworkNovel(model, networkSearchText, downloaderMarkList);
        }
        model.addAttribute("localSearchText", localSearchText);
        model.addAttribute("networkSearchText", networkSearchText);
        model.addAttribute("tabIndex", tabIndex);
        model.addAttribute("downloaderMarkList", downloaderMarkList);
        novelPageKey = "novelPageKey-" + UuidUtil.genId();
        model.addAttribute("novelPageKey", novelPageKey);
        MemoryStorage.putForever(novelPageKey, model.asMap());
        return "novel/novel";
    }

    public Model handleLocalNovel(Model model, String localSearchText) {
        log.info("{}", localSearchText);
        return model;
    }

    public Model handleNetworkNovel(Model model, String networkSearchText, List<String> downloaderMarkList) {
        Wrapper<List<QueryNetworkNovelInfo>> queryNovelWrapper = novelNetworkService.queryNovel(networkSearchText,
                downloaderMarkList != null && !downloaderMarkList.isEmpty() ? downloaderMarkList : List.of());
        if (WrapMapper.isOk(queryNovelWrapper)) {
            Map<String, QueryNetworkNovelInfo> searchResultMap = queryNovelWrapper.data().stream()
                    .collect(Collectors.toMap(QueryNetworkNovelInfo::getDownloaderMark, Function.identity()));
            String queryNetworkNovelStorageKey = "queryNetworkNovel-" + UuidUtil.genId();
            MemoryStorage.put(queryNetworkNovelStorageKey, searchResultMap);
            model.addAttribute("searchResultMap", searchResultMap);
            model.addAttribute("queryNetworkNovelStorageKey", queryNetworkNovelStorageKey);
        }
        return model;
    }

    @RequestMapping("network_novel_info")
    @ViewLog
    public String networkNovelInfo(Model model,
                                   @RequestParam("queryNetworkNovelStorageKey") String queryNetworkNovelStorageKey,
                                   @RequestParam("novelStorageKey") String novelStorageKey,
                                   @RequestParam("novelPageKey") String novelPageKey,
                                   @RequestParam(value = "queryCatalogueStorageKey", required = false)
                                   String queryCatalogueStorageKey,
                                   @RequestParam(value = "networkNovelInfoPageKey", required = false)
                                   String networkNovelInfoPageKey) {
        if (ViewUtil.checkLoaded(model, networkNovelInfoPageKey)) {
            return "novel/network/network_novel_info";
        }
        if (ViewUtil.checkIllegalParam(queryNetworkNovelStorageKey, novelStorageKey, novelPageKey)) {
            return ViewUtil.goError(model, "参数出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "novelStorageKey", novelStorageKey, "novelPageKey", novelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey));
        }

        NetworkNovelInfo novelInfo = findStorageNovelInfo(queryNetworkNovelStorageKey, novelStorageKey);
        if (novelInfo == null) {
            return ViewUtil.goError(model, "网络小说信息出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "novelStorageKey", novelStorageKey, "novelPageKey", novelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey));
        }
        List<NetworkNovelChapter> chapterList = null;
        if (StringUtils.hasText(queryCatalogueStorageKey)) {
            chapterList = MemoryStorage.get(queryCatalogueStorageKey, new TypeReference<>() {
            });
        }
        if (chapterList != null && !chapterList.isEmpty()) {
            model.addAttribute("queryCatalogueStorageKey", queryCatalogueStorageKey);
            model.addAttribute("chapterList", chapterList);
        } else {
            Wrapper<List<NetworkNovelChapter>> catalogueWrapper = novelNetworkService
                    .findCatalogue(novelInfo.getDownloaderMark(), novelInfo.getChapterUrl());
            if (WrapMapper.isOk(catalogueWrapper)) {
                queryCatalogueStorageKey = "queryCatalogueStorageKey-" + UuidUtil.genId();
                MemoryStorage.put(queryCatalogueStorageKey, catalogueWrapper.data());
                model.addAttribute("queryCatalogueStorageKey", queryCatalogueStorageKey);
                model.addAttribute("chapterList", catalogueWrapper.data());
            } else {
                model.addAttribute("chapterList", Collections.emptyList());
            }
        }
        model.addAttribute("novelInfo", novelInfo);
        model.addAttribute("novelPageKey", novelPageKey);
        model.addAttribute("queryNetworkNovelStorageKey", queryNetworkNovelStorageKey);
        model.addAttribute("novelStorageKey", novelStorageKey);
        networkNovelInfoPageKey = "networkNovelInfoPageKey-" + UuidUtil.genId();
        model.addAttribute("networkNovelInfoPageKey", networkNovelInfoPageKey);
        MemoryStorage.putForever(networkNovelInfoPageKey, model.asMap());
        return "novel/network/network_novel_info";
    }

    private NetworkNovelInfo findStorageNovelInfo(String queryNetworkNovelStorageKey,
                                                  String novelStorageKey) {
        Map<String, QueryNetworkNovelInfo> queryStorageValue =
                MemoryStorage.get(queryNetworkNovelStorageKey, new TypeReference<>() {
                });
        if (queryStorageValue == null) {
            return null;
        }
        for (QueryNetworkNovelInfo value : queryStorageValue.values()) {
            if (value.getNovelInfoList() == null || value.getNovelInfoList().isEmpty()) {
                continue;
            }
            for (NetworkNovelInfo novelInfo : value.getNovelInfoList()) {
                if (novelInfo.getStorageKey().equals(novelStorageKey)) {
                    return novelInfo;
                }
            }
        }
        return null;
    }

    @RequestMapping("network_novel_content")
    @ViewLog
    public String networkNovelContent(Model model,
                                      @RequestParam("queryNetworkNovelStorageKey") String queryNetworkNovelStorageKey,
                                      @RequestParam("novelStorageKey") String novelStorageKey,
                                      @RequestParam("novelPageKey") String novelPageKey,
                                      @RequestParam("queryCatalogueStorageKey") String queryCatalogueStorageKey,
                                      @RequestParam("chapterIndex") String chapterIndexStr,
                                      @RequestParam("networkNovelInfoPageKey") String networkNovelInfoPageKey,
                                      @RequestParam("downloaderMark") String downloaderMark) {
        if (ViewUtil.checkIllegalParam(queryNetworkNovelStorageKey, novelStorageKey, novelPageKey,
                queryCatalogueStorageKey, chapterIndexStr, networkNovelInfoPageKey, downloaderMark)) {
            return ViewUtil.goError(model, "参数出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "novelStorageKey", novelStorageKey, "novelPageKey", novelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "chapterIndex", chapterIndexStr,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey,
                    "downloaderMark", downloaderMark));
        }
        if (!chapterIndexStr.matches(Constant.Regex.NATURE_NUMBER)) {
            return ViewUtil.goError(model, "网络小说章节出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "novelStorageKey", novelStorageKey, "novelPageKey", novelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "chapterIndex", chapterIndexStr,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey,
                    "downloaderMark", downloaderMark));
        }
        int chapterIndex = Integer.parseInt(chapterIndexStr);
        NetworkNovelAroundChapter aroundChapter = findStorageNovelAroundChapter(queryCatalogueStorageKey,
                chapterIndex);
        if (aroundChapter == null) {
            return ViewUtil.goError(model, "网络小说章节出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "novelStorageKey", novelStorageKey, "novelPageKey", novelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "chapterIndex", chapterIndexStr,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey,
                    "downloaderMark", downloaderMark));
        }
        model.addAttribute("aroundChapter", aroundChapter);
        Wrapper<NetworkNovelContent> contentWrapper = novelNetworkService.findContent(
                aroundChapter.getChapter().getName(), aroundChapter.getChapter().getUrl(), downloaderMark);
        model.addAttribute("content", WrapMapper.isOk(contentWrapper) ? contentWrapper.data() : null);
        model.addAttribute("downloaderMark", downloaderMark);
        model.addAttribute("queryNetworkNovelStorageKey", queryNetworkNovelStorageKey);
        model.addAttribute("queryCatalogueStorageKey", queryCatalogueStorageKey);
        model.addAttribute("novelStorageKey", novelStorageKey);
        model.addAttribute("novelPageKey", novelPageKey);
        model.addAttribute("networkNovelInfoPageKey", networkNovelInfoPageKey);
        return "novel/network/network_novel_content";
    }

    private NetworkNovelAroundChapter findStorageNovelAroundChapter(String queryCatalogueStorageKey, int chapterIndex) {
        List<NetworkNovelChapter> chapterList = MemoryStorage.get(queryCatalogueStorageKey, new TypeReference<>() {
        });
        if (chapterList == null || chapterList.isEmpty() || chapterIndex > chapterList.size() - 1) {
            return null;
        }
        NetworkNovelAroundChapter aroundChapter = new NetworkNovelAroundChapter()
                .setChapter(chapterList.get(chapterIndex));
        if (chapterIndex - 1 >= 0) {
            aroundChapter.setPreChapter(chapterList.get(chapterIndex - 1));
        }
        if (chapterIndex + 1 <= chapterList.size() - 1) {
            aroundChapter.setNextChapter(chapterList.get(chapterIndex + 1));
        }
        return aroundChapter;
    }

    @RequestMapping("network_novel_download")
    @ViewLog
    public String networkNovelDownload(Model model,
                                       @RequestParam(value = "downloadNovelName", required = false)
                                       String downloadNovelName,
                                       @RequestParam(value = "downloadNovelAuthor", required = false)
                                       String downloadNovelAuthor,
                                       @RequestParam(value = "downloadNovelIntro", required = false)
                                       String downloadNovelIntro,
                                       @RequestParam(value = "downloadNovelImgUrl", required = false)
                                       String downloadNovelImgUrl,
                                       @RequestParam(value = "downloadNovelChapterUrl", required = false)
                                       String downloadNovelChapterUrl,
                                       @RequestParam(value = "downloaderMark", required = false)
                                       String downloaderMark,
                                       @RequestParam(value = "downloadStatus", required = false)
                                       String downloadStatusStr) {

        if (StringUtils.hasText(downloadStatusStr)) {
            if (!downloadStatusStr.matches(Constant.Regex.POSITIVE_INTEGER)) {
                return ViewUtil.goParamError(model, KeyValuePair.of("downloadStatus", downloadStatusStr));
            }
            int downloadStatus = Integer.parseInt(downloadStatusStr);
            if (downloadStatus == Constant.Novel.DOWNLOAD_ACTION_ASK) {
                model.addAttribute("askDownloadNovel", new NetworkNovelInfo()
                        .setName(downloadNovelName)
                        .setAuthor(downloadNovelAuthor)
                        .setIntro(downloadNovelIntro)
                        .setImgUrl(downloadNovelImgUrl)
                        .setChapterUrl(downloadNovelChapterUrl)
                        .setDownloaderMark(downloaderMark));
            }
            if (downloadStatus == Constant.Novel.DOWNLOAD_ACTION_DO) {
                NetworkNovelInfo novelInfo = new NetworkNovelInfo()
                        .setName(downloadNovelName)
                        .setAuthor(downloadNovelAuthor)
                        .setIntro(downloadNovelIntro)
                        .setImgUrl(downloadNovelImgUrl)
                        .setChapterUrl(downloadNovelChapterUrl)
                        .setDownloaderMark(downloaderMark);
                String itemIndex = MemoryStorage.putList(Constant.Novel.DOWNLOADING_STORAGE_KEY, novelInfo);
                Thread.ofVirtual().name("novelDownload-", 0)
                        .start(() -> {
                            novelNetworkService.downloadNovel(downloaderMark, novelInfo);
                            MemoryStorage.removeListItem(Constant.Novel.DOWNLOADING_STORAGE_KEY, itemIndex);
                        });
                model.addAttribute("downloadStart", true);
            }
        }
        List<NetworkNovelInfo> downloadingNovelList = MemoryStorage.getList(Constant.Novel.DOWNLOADING_STORAGE_KEY,
                new TypeReference<>() {
                });
        model.addAttribute("downloadingNovelList", downloadingNovelList);

        Wrapper<List<Novel>> downloadedNovelWrapper = novelLocalService.findNovel(null);
        model.addAttribute("downloadedNovelList", WrapMapper.isOk(downloadedNovelWrapper) ?
                downloadedNovelWrapper.data() : null);
        return "novel/network/network_novel_download";
    }
}
