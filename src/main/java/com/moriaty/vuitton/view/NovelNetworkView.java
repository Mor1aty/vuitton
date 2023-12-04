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
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloaderFactory;
import com.moriaty.vuitton.service.novel.NovelLocalService;
import com.moriaty.vuitton.service.novel.NovelNetworkService;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.service.novel.NovelService;
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
 * 网络小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午3:43
 */
@Controller
@RequestMapping("novel/network")
@AllArgsConstructor
@Slf4j
public class NovelNetworkView implements InitializingBean {

    private final NovelNetworkService novelNetworkService;

    private final NovelLocalService novelLocalService;

    private final NovelService novelService;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(2)
                .setName("网络小说")
                .setPath("novel/network"));
    }

    @RequestMapping
    @ViewLog
    public String networkNovel(Model model,
                               @RequestParam(value = "searchText", required = false) String searchText,
                               @RequestParam(value = "downloaderMark", required = false) List<String> downloaderMarkList,
                               @RequestParam(value = "tabIndex", required = false) String tabIndex,
                               @RequestParam(value = "networkNovelPageKey", required = false) String networkNovelPageKey) {
        if (ViewUtil.checkLoaded(model, networkNovelPageKey)) {
            return "novel/network/network_novel";
        }
        model.addAttribute("searchText", searchText);
        model.addAttribute("downloaderMarkList", downloaderMarkList);
        model.addAttribute("downloaderList", NovelDownloaderFactory.getAllDownloaderInfo());
        if (StringUtils.hasText(searchText) && downloaderMarkList != null && !downloaderMarkList.isEmpty()) {
            handleNetworkNovel(model, searchText, downloaderMarkList);
        }
        return "novel/network/network_novel";
    }

    public void handleNetworkNovel(Model model, String searchText, List<String> downloaderMarkList) {
        Wrapper<List<QueryNetworkNovelInfo>> queryNovelWrapper = novelNetworkService.queryNovel(searchText,
                downloaderMarkList);
        if (WrapMapper.isFailure(queryNovelWrapper)) {
            return;
        }
        Map<String, QueryNetworkNovelInfo> searchResultMap = queryNovelWrapper.data().stream()
                .collect(Collectors.toMap(QueryNetworkNovelInfo::getDownloaderMark, Function.identity()));
        String queryNetworkNovelStorageKey = "queryNetworkNovel-" + UuidUtil.genId();
        MemoryStorage.put(queryNetworkNovelStorageKey, searchResultMap);
        model.addAttribute("searchResultMap", searchResultMap);
        model.addAttribute("queryNetworkNovelStorageKey", queryNetworkNovelStorageKey);
        Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
        model.addAttribute("defaultImgUrl",
                WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
        String networkNovelPageKey = "networkNovelPageKey-" + UuidUtil.genId();
        model.addAttribute("networkNovelPageKey", networkNovelPageKey);
        MemoryStorage.putForever(networkNovelPageKey, model.asMap());
    }

    @RequestMapping("novel_info")
    @ViewLog
    public String networkNovelInfo(Model model,
                                   @RequestParam("queryNetworkNovelStorageKey") String queryNetworkNovelStorageKey,
                                   @RequestParam("networkNovelStorageKey") String networkNovelStorageKey,
                                   @RequestParam("networkNovelPageKey") String networkNovelPageKey,
                                   @RequestParam(value = "queryCatalogueStorageKey", required = false)
                                   String queryCatalogueStorageKey,
                                   @RequestParam(value = "networkNovelInfoPageKey", required = false)
                                   String networkNovelInfoPageKey,
                                   @RequestParam(value = "chapterOrder", required = false)
                                   String chapterOrderStr) {
        if (ViewUtil.checkLoaded(model, networkNovelInfoPageKey)) {
            log.info("defaultImgUrl {}", model.getAttribute("defaultImgUrl"));
            return "novel/network/network_novel_info";
        }
        if (ViewUtil.checkIllegalParam(queryNetworkNovelStorageKey, networkNovelStorageKey, networkNovelPageKey)) {
            return ViewUtil.goError(model, "参数出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "networkNovelStorageKey", networkNovelStorageKey, "networkNovelPageKey", networkNovelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey));
        }
        model.addAttribute("chapterOrder", chapterOrderStr);
        NetworkNovelInfo novelInfo = findStorageNovelInfo(queryNetworkNovelStorageKey, networkNovelStorageKey);
        if (novelInfo == null) {
            return ViewUtil.goError(model, "网络小说信息出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "networkNovelStorageKey", networkNovelStorageKey, "networkNovelPageKey", networkNovelPageKey,
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
                    .findCatalogue(novelInfo.getDownloaderMark(), novelInfo.getCatalogueUrl(),
                            StringUtils.hasText(chapterOrderStr)
                                    && chapterOrderStr.equals(String.valueOf(Constant.Novel.CHAPTER_ORDER_DESC)));
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
        model.addAttribute("networkNovelPageKey", networkNovelPageKey);
        model.addAttribute("queryNetworkNovelStorageKey", queryNetworkNovelStorageKey);
        model.addAttribute("networkNovelStorageKey", networkNovelStorageKey);
        if (!StringUtils.hasText(novelInfo.getImgUrl())) {
            Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
            model.addAttribute("defaultImgUrl",
                    WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
        }
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

    @RequestMapping("novel_content")
    @ViewLog
    public String networkNovelContent(Model model,
                                      @RequestParam("queryNetworkNovelStorageKey") String queryNetworkNovelStorageKey,
                                      @RequestParam("networkNovelStorageKey") String networkNovelStorageKey,
                                      @RequestParam("networkNovelPageKey") String networkNovelPageKey,
                                      @RequestParam("queryCatalogueStorageKey") String queryCatalogueStorageKey,
                                      @RequestParam("chapterIndex") String chapterIndexStr,
                                      @RequestParam("networkNovelInfoPageKey") String networkNovelInfoPageKey,
                                      @RequestParam("downloaderMark") String downloaderMark) {
        if (ViewUtil.checkIllegalParam(queryNetworkNovelStorageKey, networkNovelStorageKey, networkNovelPageKey,
                queryCatalogueStorageKey, chapterIndexStr, networkNovelInfoPageKey, downloaderMark)) {
            return ViewUtil.goError(model, "参数出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "networkNovelStorageKey", networkNovelStorageKey, "networkNovelPageKey", networkNovelPageKey,
                    "queryCatalogueStorageKey", queryCatalogueStorageKey,
                    "chapterIndex", chapterIndexStr,
                    "networkNovelInfoPageKey", networkNovelInfoPageKey,
                    "downloaderMark", downloaderMark));
        }
        if (!chapterIndexStr.matches(Constant.Regex.NATURE_NUMBER)) {
            return ViewUtil.goError(model, "网络小说章节出问题啦", KeyValuePair.ofList(
                    "queryNetworkNovelStorageKey", queryNetworkNovelStorageKey,
                    "networkNovelStorageKey", networkNovelStorageKey, "networkNovelPageKey", networkNovelPageKey,
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
                    "networkNovelStorageKey", networkNovelStorageKey, "networkNovelPageKey", networkNovelPageKey,
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
        model.addAttribute("networkNovelStorageKey", networkNovelStorageKey);
        model.addAttribute("networkNovelPageKey", networkNovelPageKey);
        model.addAttribute("networkNovelInfoPageKey", networkNovelInfoPageKey);
        Wrapper<List<Setting>> settingWrapper = novelService.findSetting(null,
                Constant.Setting.NOVEL_CONTENT_FONT_SIZE);
        model.addAttribute("fontSizeSetting",
                WrapMapper.isFailure(settingWrapper) || settingWrapper.data().isEmpty() ?
                        null : settingWrapper.data().getFirst());
        return "novel/network/network_novel_content";
    }

    private NetworkNovelAroundChapter findStorageNovelAroundChapter(String queryCatalogueStorageKey, int chapterIndex) {
        List<NetworkNovelChapter> chapterList = MemoryStorage.get(queryCatalogueStorageKey, new TypeReference<>() {
        });
        if (chapterList == null || chapterList.isEmpty()) {
            return null;
        }
        for (int i = 0; i < chapterList.size(); i++) {
            NetworkNovelChapter novelChapter = chapterList.get(i);
            if (novelChapter.getIndex() == chapterIndex) {
                return handleStorageNovelAroundChapter(novelChapter, i, chapterList);
            }
        }
        return null;
    }

    private NetworkNovelAroundChapter handleStorageNovelAroundChapter(NetworkNovelChapter novelChapter,
                                                                      int index,
                                                                      List<NetworkNovelChapter> chapterList) {
        NetworkNovelAroundChapter aroundChapter = new NetworkNovelAroundChapter().setChapter(novelChapter);
        NetworkNovelChapter upChapter = index - 1 >= 0 ? chapterList.get(index - 1) : null;
        NetworkNovelChapter downChapter = index + 1 <= chapterList.size() - 1 ?
                chapterList.get(index + 1) : null;
        if (upChapter != null) {
            if (upChapter.getIndex() < novelChapter.getIndex()) {
                aroundChapter.setPreChapter(upChapter);
            } else {
                aroundChapter.setNextChapter(upChapter);
            }
        }
        if (downChapter != null) {
            if (downChapter.getIndex() > novelChapter.getIndex()) {
                aroundChapter.setNextChapter(downChapter);
            } else {
                aroundChapter.setPreChapter(downChapter);
            }
        }
        return aroundChapter;
    }

    @RequestMapping("novel_download")
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
                                       @RequestParam(value = "downloadNovelCatalogueUrl", required = false)
                                       String downloadNovelCatalogueUrl,
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
                        .setCatalogueUrl(downloadNovelCatalogueUrl)
                        .setDownloaderMark(downloaderMark));
            }
            if (downloadStatus == Constant.Novel.DOWNLOAD_ACTION_DO) {
                NetworkNovelInfo novelInfo = new NetworkNovelInfo()
                        .setName(downloadNovelName)
                        .setAuthor(downloadNovelAuthor)
                        .setIntro(downloadNovelIntro)
                        .setImgUrl(downloadNovelImgUrl)
                        .setCatalogueUrl(downloadNovelCatalogueUrl)
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

        Wrapper<List<Novel>> downloadedNovelWrapper = novelLocalService.findNovel(null, null);
        model.addAttribute("downloadedNovelList", WrapMapper.isOk(downloadedNovelWrapper) ?
                downloadedNovelWrapper.data() : null);
        Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
        model.addAttribute("defaultImgUrl",
                WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
        return "novel/network/network_novel_download";
    }
}
