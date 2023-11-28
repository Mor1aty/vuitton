package com.moriaty.vuitton.view;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
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
                        .collect(Collectors.toMap(QueryNetworkNovelInfo::getSourceMark, Function.identity()));
                String queryStorageKey = "queryNetworkNovel-" + UuidUtil.genId();
                MemoryStorage.put(queryStorageKey, searchResultMap);
                model.addAttribute("searchResultMap", searchResultMap);
                model.addAttribute("queryStorageKey", queryStorageKey);
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
                                   @RequestParam("queryStorageKey") String queryStorageKey,
                                   @RequestParam("novelStorageKey") String novelStorageKey,
                                   @RequestParam("novelPageKey") String novelPageKey) {
        if (!StringUtils.hasText(queryStorageKey)
            || !StringUtils.hasText(novelStorageKey)
            || !StringUtils.hasText(novelPageKey)) {
            return ViewUtil.goError(model, "网络小说信息出问题啦", "queryStorageKey=" + queryStorageKey
                                                                   + ", novelStorageKey=" + novelStorageKey
                                                                   + ", novelPageKey=" + novelPageKey);
        }
        NetworkNovelInfo novelInfo = findNovelInfoFromMemoryStorage(queryStorageKey, novelStorageKey);
        if (novelInfo == null) {
            return ViewUtil.goError(model, "网络小说信息出问题啦", "queryStorageKey=" + queryStorageKey
                                                                   + ", novelStorageKey=" + novelStorageKey
                                                                   + ", novelPageKey=" + novelPageKey);
        }
        model.addAttribute("novelInfo", novelInfo);
        model.addAttribute("novelPageKey", novelPageKey);
        return "novel/network/network_novel_info";
    }

    private NetworkNovelInfo findNovelInfoFromMemoryStorage(String queryStorageKey, String novelStorageKey) {
        Map<String, QueryNetworkNovelInfo> queryStorageValue =
                MemoryStorage.get(queryStorageKey, new TypeReference<>() {
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
}
