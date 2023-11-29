package com.moriaty.vuitton.view;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.novel.NovelFactory;
import com.moriaty.vuitton.service.novel.NovelNetworkService;
import com.moriaty.vuitton.util.UuidUtil;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Module(id = 0, name = "小说", path = "novel")
public class NovelView {

    private final NovelNetworkService novelNetworkService;

    @RequestMapping
    public String novel(Model model,
                        @RequestParam(value = "localSearchText", required = false) String localSearchText,
                        @RequestParam(value = "networkSearchText", required = false) String networkSearchText,
                        @RequestParam(value = "downloaderMark", required = false) List<String> downloaderMarkList,
                        @RequestParam(value = "tabIndex", required = false) String tabIndex,
                        @RequestParam(value = "novelPageKey", required = false) String novelPageKey) {
        if (StringUtils.hasText(novelPageKey)) {
            Map<String, Object> modelAttrMap = MemoryStorage.get(novelPageKey, new TypeReference<>() {
            });
            if (modelAttrMap != null) {
                model.addAllAttributes(modelAttrMap);
                return "novel/novel";
            }
        }
        model.addAttribute("downloaderList", NovelFactory.getAllDownloaderInfo());
        if (StringUtils.hasText(localSearchText)) {
            log.info("{}", localSearchText);
        }
        if (StringUtils.hasText(networkSearchText)) {
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

    @RequestMapping("network_novel_info")
    public String networkNovelInfo(Model model,
                                   @RequestParam("queryNetworkNovelStorageKey") String queryNetworkNovelStorageKey,
                                   @RequestParam("novelStorageKey") String novelStorageKey,
                                   @RequestParam("novelPageKey") String novelPageKey,
                                   @RequestParam(value = "queryCatalogueStorageKey", required = false)
                                   String queryCatalogueStorageKey,
                                   @RequestParam(value = "networkNovelInfoPageKey", required = false)
                                   String networkNovelInfoPageKey) {
        if (StringUtils.hasText(networkNovelInfoPageKey)) {
            Map<String, Object> modelAttrMap = MemoryStorage.get(networkNovelInfoPageKey, new TypeReference<>() {
            });
            if (modelAttrMap != null) {
                model.addAllAttributes(modelAttrMap);
                return "novel/network/network_novel_info";
            }
        }
        if (!StringUtils.hasText(queryNetworkNovelStorageKey)
            || !StringUtils.hasText(novelStorageKey)
            || !StringUtils.hasText(novelPageKey)) {
            return ViewUtil.goError(model, "网络小说出问题啦", "queryNetworkNovelStorageKey="
                                                                   + queryNetworkNovelStorageKey
                                                                   + ", novelStorageKey=" + novelStorageKey
                                                                   + ", novelPageKey=" + novelPageKey);
        }
        NetworkNovelInfo novelInfo = findNovelInfoFromMemoryStorage(queryNetworkNovelStorageKey, novelStorageKey);
        if (novelInfo == null) {
            return ViewUtil.goError(model, "网络小说信息出问题啦", "queryNetworkNovelStorageKey="
                                                                   + queryNetworkNovelStorageKey
                                                                   + ", novelStorageKey=" + novelStorageKey
                                                                   + ", novelPageKey=" + novelPageKey);
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
        networkNovelInfoPageKey = "networkNovelInfoPageKey-" + UuidUtil.genId();
        model.addAttribute("networkNovelInfoPageKey", networkNovelInfoPageKey);
        MemoryStorage.putForever(networkNovelInfoPageKey, model.asMap());
        return "novel/network/network_novel_info";
    }

    @RequestMapping("network_novel_content")
    public String networkNovelInfo(Model model,
                                   @RequestParam("queryCatalogueStorageKey") String queryCatalogueStorageKey,
                                   @RequestParam("chapterIndex") String chapterIndexStr,
                                   @RequestParam("networkNovelInfoPageKey") String networkNovelInfoPageKey,
                                   @RequestParam("downloaderMark") String downloaderMark) {
        if (!StringUtils.hasText(queryCatalogueStorageKey)
            || !StringUtils.hasText(chapterIndexStr)
            || !StringUtils.hasText(networkNovelInfoPageKey)
            || !StringUtils.hasText(downloaderMark)) {
            return ViewUtil.goError(model, "网络小说内容出问题啦", "queryCatalogueStorageKey="
                                                                   + queryCatalogueStorageKey
                                                                   + ", chapterIndex=" + chapterIndexStr
                                                                   + ", networkNovelInfoPageKey="
                                                                   + networkNovelInfoPageKey
                                                                   + ", downloaderMark=" + downloaderMark);
        }
        if (!chapterIndexStr.matches(Constant.Regex.NATURE_NUMBER)) {
            return ViewUtil.goError(model, "网络小说章节出问题啦", "queryCatalogueStorageKey="
                                                                   + queryCatalogueStorageKey
                                                                   + ", chapterIndex=" + chapterIndexStr
                                                                   + ", networkNovelInfoPageKey="
                                                                   + networkNovelInfoPageKey
                                                                   + ", downloaderMark=" + downloaderMark);
        }
        int chapterIndex = Integer.parseInt(chapterIndexStr);
        NetworkNovelChapter chapter = findNovelChapterFromMemoryStorage(queryCatalogueStorageKey, chapterIndex);
        if (chapter == null) {
            return ViewUtil.goError(model, "网络小说章节出问题啦", "queryCatalogueStorageKey="
                                                                   + queryCatalogueStorageKey
                                                                   + ", chapterIndex=" + chapterIndexStr
                                                                   + ", networkNovelInfoPageKey="
                                                                   + networkNovelInfoPageKey
                                                                   + ", downloaderMark=" + downloaderMark);
        }
        model.addAttribute("chapter", chapter);
        Wrapper<NetworkNovelContent> contentWrapper = novelNetworkService.findContent(chapter.getName(),
                chapter.getUrl(), downloaderMark);
        model.addAttribute("content", WrapMapper.isOk(contentWrapper) ? contentWrapper.data() : null);
        model.addAttribute("networkNovelInfoPageKey", networkNovelInfoPageKey);
        return "novel/network/network_novel_content";
    }

    private NetworkNovelInfo findNovelInfoFromMemoryStorage(String queryNetworkNovelStorageKey,
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

    private NetworkNovelChapter findNovelChapterFromMemoryStorage(String queryCatalogueStorageKey, int chapterIndex) {
        List<NetworkNovelChapter> chapterList = MemoryStorage.get(queryCatalogueStorageKey, new TypeReference<>() {
        });
        if (chapterList == null || chapterList.isEmpty() || chapterIndex > chapterList.size() - 1) {
            return null;
        }
        return chapterList.get(chapterIndex);
    }
}
