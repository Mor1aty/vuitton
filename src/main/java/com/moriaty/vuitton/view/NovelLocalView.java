package com.moriaty.vuitton.view;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.bean.novel.local.NovelCheckChapter;
import com.moriaty.vuitton.bean.novel.local.NovelReadHistoryInfo;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.module.novel.NovelLocalModule;
import com.moriaty.vuitton.module.novel.NovelModule;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloaderFactory;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.util.UuidUtil;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 本地小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 2:46
 */
@Controller
@RequestMapping("novel/local")
@RequiredArgsConstructor
@Slf4j
public class NovelLocalView {

    private final NovelLocalModule novelLocalModule;

    private final NovelModule novelModule;

    @Value("${file-server.novel.default-novel-img}")
    private String defaultImgUrl;

    @RequestMapping
    @ViewLog
    public String localNovel(Model model, @RequestParam(value = "searchText", required = false) String searchText) {
        List<Novel> novelList = novelLocalModule.findNovel(null, searchText);
        model.addAttribute("novelList", novelList);
        model.addAttribute("searchText", searchText);
        model.addAttribute("defaultImgUrl", defaultImgUrl);
        return "novel/local/local_novel";
    }

    @RequestMapping("novel_info")
    @ViewLog
    public String localNovelInfo(Model model, @RequestParam("novelId") String novelId,
                                 @RequestParam(value = "chapterOrder", required = false) String chapterOrderStr) {
        Novel novel = findNovelById(novelId);
        if (novel == null) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        model.addAttribute("novel", novel);
        List<NovelChapter> chapterList = novelLocalModule.findCatalogue(novelId,
                StringUtils.hasText(chapterOrderStr)
                        && chapterOrderStr.equals(String.valueOf(Constant.Novel.CHAPTER_ORDER_DESC)));
        model.addAttribute("chapterList", chapterList);
        List<NovelReadHistoryInfo> readHistory = novelLocalModule.findNovelReadHistory(novelId);
        model.addAttribute("readHistory", readHistory.isEmpty() ? null : readHistory.getFirst());
        if (!StringUtils.hasText(novel.getImgUrl())) {
            model.addAttribute("defaultImgUrl", defaultImgUrl);
        }
        model.addAttribute("chapterOrder", chapterOrderStr);
        return "novel/local/local_novel_info";
    }

    @RequestMapping("downloadLocalNovel")
    public ResponseEntity<Resource> downloadLocalNovel(@RequestParam("novelFile") String novelFile,
                                                       @RequestParam("novelName") String novelName) {
        Resource file = new FileSystemResource(novelFile);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("[V]" + URLEncoder.encode(novelName, StandardCharsets.UTF_8) + ".txt").build());
        return ResponseEntity.ok().headers(headers).body(file);
    }

    @RequestMapping("novel_content")
    @ViewLog
    public String localNovelContent(Model model, @RequestParam("novelId") String novelId,
                                    @RequestParam("chapterIndex") String chapterIndexStr) {
        if (ViewUtil.checkIllegalParam(Collections.emptyList(),
                () -> !chapterIndexStr.matches(Constant.Regex.NATURE_NUMBER))) {
            return ViewUtil.goParamError(model,
                    KeyValuePair.ofList("novelId", novelId, "chapterIndex", chapterIndexStr));
        }
        model.addAttribute("novelId", novelId);
        int chapterIndex = Integer.parseInt(chapterIndexStr);
        LocalNovelAroundChapter aroundChapter = novelLocalModule.findAroundChapter(novelId, chapterIndex);
        model.addAttribute("aroundChapter", aroundChapter);
        novelLocalModule.insertNovelReadHistory(novelId, chapterIndex);
        List<Setting> settingList = novelModule.findSetting(null,
                Constant.Setting.NOVEL_CONTENT_FONT_SIZE);
        model.addAttribute("fontSizeSetting", settingList.isEmpty() ? null : settingList.getFirst());
        return "novel/local/local_novel_content";
    }

    @RequestMapping("novel_check")
    @ViewLog
    public String localNovelCheck(Model model, @RequestParam("novelId") String novelId,
                                  @RequestParam(value = "novelCheckAction", required = false)
                                  String novelCheckActionStr,
                                  @RequestParam(value = "fillUpNovelSearchKey", required = false)
                                  String fillUpNovelSearchKey,
                                  @RequestParam(value = "fillUpNovelStorageKey", required = false)
                                  String fillUpNovelStorageKey) {

        Novel novel = findNovelById(novelId);
        if (novel == null) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        model.addAttribute("novel", novel);
        model.addAttribute("defaultImgUrl", defaultImgUrl);

        int novelCheckAction = StringUtils.hasText(novelCheckActionStr)
                && novelCheckActionStr.matches(Constant.Regex.NATURE_NUMBER) ?
                Integer.parseInt(novelCheckActionStr) : 0;
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_ASK) {
            askNovelCheck(model, novel, defaultImgUrl);
        }
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_RECOVER) {
            recoverNovelCheck(model, novelId);
        }
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_FILL_UP) {
            if (ViewUtil.checkIllegalParam(List.of(fillUpNovelSearchKey, fillUpNovelStorageKey))) {
                return ViewUtil.goParamError(model, KeyValuePair.ofList("novelId", novelId,
                        "novelCheckAction", novelCheckActionStr, "fillUpNovelSearchKey", fillUpNovelSearchKey,
                        "fillUpNovelStorageKey", fillUpNovelStorageKey));
            }
            fillUpNovel(model, novelId, fillUpNovelSearchKey, fillUpNovelStorageKey);
        }
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_RESET_SEQ) {
            resetSeqNovelChapter(model, novel);
        }
        List<Integer> checkingChapterList = MemoryStorage.getList(Constant.Novel.CHECKING_STORAGE_KEY,
                new TypeReference<>() {
                });
        model.addAttribute("checkingChapterList", checkingChapterList);
        return "novel/local/local_novel_check";
    }

    private List<NovelCheckChapter> checkNovelChapter(String novelId) {
        List<NovelChapter> chapterList = novelLocalModule.findCatalogue(novelId, false);
        List<NovelCheckChapter> lossChapterChapterList = new ArrayList<>();
        int currentChapterIndex = 0;
        for (int i = 0; i < chapterList.size(); i++) {
            NovelChapter chapter = chapterList.get(i);

            NovelChapter preNearChapter = null;
            if (i - 1 >= 0) {
                preNearChapter = chapterList.get(i - 1);
            }
            int lossNum = chapter.getIndex() - currentChapterIndex;
            for (int j = 0; j < lossNum; j++) {
                lossChapterChapterList.add(new NovelCheckChapter()
                        .setChapterIndex(currentChapterIndex)
                        .setPreNearChapter(preNearChapter)
                        .setNextNearChapter(chapter));
                currentChapterIndex++;
            }
            currentChapterIndex++;
        }
        return lossChapterChapterList;
    }

    private List<NetworkNovelInfo> checkNovel(Novel novel, String defaultImgUrl) {
        if (StringUtils.hasText(novel.getIntro()) && StringUtils.hasText(novel.getImgUrl())
                && !novel.getImgUrl().equals(defaultImgUrl)) {
            return Collections.emptyList();
        }
        List<NovelDownloader> downloaderList = NovelDownloaderFactory.getAllDownloader();
        List<NetworkNovelInfo> fillUpNovelList = new ArrayList<>();
        downloaderList.parallelStream().forEach(downloader -> {
            QueryNetworkNovelInfo queryNetworkNovelInfo = downloader.queryNovel(novel.getName());
            if (queryNetworkNovelInfo.getNovelInfoList() != null) {
                fillUpNovelList.addAll(queryNetworkNovelInfo.getNovelInfoList());
            }
        });
        return fillUpNovelList;
    }

    private NetworkNovelInfo findFillUpNovel(String fillUpNovelSearchKey, String fillUpNovelStorageKey) {
        List<NetworkNovelInfo> fillUpNovelList = MemoryStorage.get(fillUpNovelSearchKey, new TypeReference<>() {
        });
        if (fillUpNovelList == null || fillUpNovelList.isEmpty()) {
            return null;
        }
        for (NetworkNovelInfo novelInfo : fillUpNovelList) {
            if (novelInfo.getStorageKey().equals(fillUpNovelStorageKey)
                    && StringUtils.hasText(novelInfo.getIntro()) && StringUtils.hasText(novelInfo.getImgUrl())) {
                return novelInfo;
            }
        }
        return null;
    }

    private void askNovelCheck(Model model, Novel novel, String defaultImgUrl) {
        List<NovelCheckChapter> lossNovelChapterList = checkNovelChapter(novel.getId());
        model.addAttribute("lossNovelChapterList", lossNovelChapterList);
        List<NetworkNovelInfo> fillUpNovelList = checkNovel(novel, defaultImgUrl);
        if (!fillUpNovelList.isEmpty()) {
            String fillUpNovelSearchKey = Constant.Novel.FILL_UP_STORAGE_KEY + "-" + UuidUtil.genId();
            MemoryStorage.put(fillUpNovelSearchKey, fillUpNovelList);
            model.addAttribute("fillUpNovelSearchKey", fillUpNovelSearchKey);
            model.addAttribute("fillUpNovelList", fillUpNovelList);
        }
        if (!lossNovelChapterList.isEmpty()) {
            model.addAttribute("askResetSeq", true);
        }
    }

    private void recoverNovelCheck(Model model, String novelId) {
        List<Integer> checkingChapterIndexList = checkNovelChapter(novelId).stream()
                .map(NovelCheckChapter::getChapterIndex).toList();
        if (!checkingChapterIndexList.isEmpty()) {
            checkingChapterIndexList.forEach(checkingChapterIndex -> {
                String itemIndex = MemoryStorage.putList(Constant.Novel.CHECKING_STORAGE_KEY, checkingChapterIndex);
                Thread.ofVirtual().name("novelCheck-", 0)
                        .start(() -> {
                            boolean isSuccess =
                                    novelLocalModule.recoverNovelChapter(novelId, checkingChapterIndex);
                            log.info("恢复 {} {} {}", novelId, checkingChapterIndex, isSuccess ? "成功" : "失败");
                            MemoryStorage.removeListItem(Constant.Novel.CHECKING_STORAGE_KEY, itemIndex);
                        });
            });
        }
        model.addAttribute("checkingNovelStart", true);
    }

    private void fillUpNovel(Model model, String novelId, String fillUpNovelSearchKey, String fillUpNovelStorageKey) {
        NetworkNovelInfo fillUpNovel = findFillUpNovel(fillUpNovelSearchKey, fillUpNovelStorageKey);
        if (fillUpNovel != null) {
            novelLocalModule.updateNovel(new Novel()
                    .setId(novelId)
                    .setIntro(fillUpNovel.getIntro())
                    .setImgUrl(fillUpNovel.getImgUrl()));
            log.info("补充小说信息成功");
        } else {
            log.info("补充小说信息失败");
        }
        model.addAttribute("checkingNovelStart", true);
    }

    private void resetSeqNovelChapter(Model model, Novel novel) {
        model.addAttribute("checkingNovelStart", true);
        List<NovelChapter> chapterList = novelLocalModule.findCatalogue(novel.getId(), false);
        for (int i = 0; i < chapterList.size(); i++) {
            chapterList.set(i, chapterList.get(i).setIndex(i));
        }
        novelLocalModule.deleteCatalogue(novel.getId());
        log.info("删除目录成功");
        novelLocalModule.insertCatalogue(chapterList);
        log.info("插入目录成功");
        novelLocalModule.deleteNovelReadHistory(novel.getId());
        log.info("删除阅读历史记录成功");
    }

    @RequestMapping("novel_delete")
    @ViewLog
    public String localNovelDelete(Model model, @RequestParam("novelId") String novelId) {
        Novel novel = findNovelById(novelId);
        if (novel == null) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        model.addAttribute("novel", novel);
        if (!StringUtils.hasText(novel.getImgUrl())) {
            model.addAttribute("defaultImgUrl", defaultImgUrl);
        }
        return "novel/local/local_novel_delete";
    }

    private Novel findNovelById(String novelId) {
        List<Novel> novelList = novelLocalModule.findNovel(novelId, null);
        if (novelList.isEmpty()) {
            return null;
        }
        return novelList.getFirst();
    }

    @RequestMapping("action_delete_novel")
    @ViewLog
    public String actionAddVideo(Model model, @RequestParam("novelId") String novelId) {
        Novel novel = findNovelById(novelId);
        if (novel == null) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        boolean isSuccess = novelLocalModule.deleteNovel(novel);
        model.addAttribute("actionMsg", "删除小说" + (isSuccess ? "成功" : "失败"));
        model.addAttribute("backUrl", "/novel/local");
        return "action_result";
    }

}
