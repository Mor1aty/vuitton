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
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloaderFactory;
import com.moriaty.vuitton.service.novel.NovelLocalService;
import com.moriaty.vuitton.service.novel.NovelService;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.util.UuidUtil;
import com.moriaty.vuitton.util.ViewUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
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
@AllArgsConstructor
@Slf4j
public class NovelLocalView implements InitializingBean {

    private final NovelLocalService novelLocalService;

    private final NovelService novelService;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(1)
                .setName("本地小说")
                .setPath("novel/local"));
    }

    @RequestMapping
    @ViewLog
    public String localNovel(Model model,
                             @RequestParam(value = "searchText", required = false) String searchText) {
        Wrapper<List<Novel>> novelWrapper = novelLocalService.findNovel(null, searchText);
        model.addAttribute("novelList", WrapMapper.isOk(novelWrapper) ?
                novelWrapper.data() : Collections.emptyList());
        model.addAttribute("searchText", searchText);
        Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
        model.addAttribute("defaultImgUrl",
                WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
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
        Wrapper<List<NovelChapter>> catalogueWrapper = novelLocalService.findCatalogue(novelId,
                StringUtils.hasText(chapterOrderStr)
                        && chapterOrderStr.equals(String.valueOf(Constant.Novel.CHAPTER_ORDER_DESC)));
        model.addAttribute("chapterList",
                WrapMapper.isOk(catalogueWrapper) ? catalogueWrapper.data() : Collections.emptyList());
        Wrapper<List<NovelReadHistoryInfo>> readHistoryWrapper = novelLocalService.findNovelReadHistory(novelId);
        model.addAttribute("readHistory",
                WrapMapper.isFailure(readHistoryWrapper) || readHistoryWrapper.data().isEmpty() ?
                        null : readHistoryWrapper.data().getFirst());
        if (!StringUtils.hasText(novel.getImgUrl())) {
            Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
            model.addAttribute("defaultImgUrl",
                    WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
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
                .filename(URLEncoder.encode(novelName, StandardCharsets.UTF_8) + ".txt").build());
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
        Wrapper<LocalNovelAroundChapter> aroundChapterWrapper = novelLocalService.findAroundChapter(novelId, chapterIndex);
        model.addAttribute("aroundChapter", WrapMapper.isOk(aroundChapterWrapper) ?
                aroundChapterWrapper.data() : new LocalNovelAroundChapter());
        Wrapper<Void> insertWrapper = novelLocalService.insertNovelReadHistory(novelId, chapterIndex);
        if (WrapMapper.isFailure(insertWrapper)) {
            return ViewUtil.goError(model, "插入阅读记录出问题啦", KeyValuePair.ofList(
                    "novelId", novelId, "chapterIndex", chapterIndexStr));
        }
        Wrapper<List<Setting>> settingWrapper = novelService.findSetting(null,
                Constant.Setting.NOVEL_CONTENT_FONT_SIZE);
        model.addAttribute("fontSizeSetting",
                WrapMapper.isFailure(settingWrapper) || settingWrapper.data().isEmpty() ?
                        null : settingWrapper.data().getFirst());
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
        Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
        String defaultImgUrl = WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null;
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
        Wrapper<List<NovelChapter>> catalogueWrapper = novelLocalService.findCatalogue(novelId, false);
        if (WrapMapper.isFailure(catalogueWrapper)) {
            return Collections.emptyList();
        }
        List<NovelCheckChapter> lossChapterChapterList = new ArrayList<>();
        int currentChapterIndex = 0;
        for (int i = 0; i < catalogueWrapper.data().size(); i++) {
            NovelChapter chapter = catalogueWrapper.data().get(i);

            NovelChapter preNearChapter = null;
            if (i - 1 >= 0) {
                preNearChapter = catalogueWrapper.data().get(i - 1);
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
                            Wrapper<Void> recoverWrapper =
                                    novelLocalService.recoverNovelChapter(novelId, checkingChapterIndex);
                            log.info("恢复 {} {} {}", novelId, checkingChapterIndex,
                                    WrapMapper.isOk(recoverWrapper) ? "成功" : "失败, " + recoverWrapper.msg());
                            MemoryStorage.removeListItem(Constant.Novel.CHECKING_STORAGE_KEY, itemIndex);
                        });
            });
        }
        model.addAttribute("checkingNovelStart", true);
    }

    private void fillUpNovel(Model model, String novelId, String fillUpNovelSearchKey, String fillUpNovelStorageKey) {
        NetworkNovelInfo fillUpNovel = findFillUpNovel(fillUpNovelSearchKey, fillUpNovelStorageKey);
        if (fillUpNovel != null) {
            Wrapper<Void> updateWrapper = novelLocalService.updateNovel(new Novel()
                    .setId(novelId)
                    .setIntro(fillUpNovel.getIntro())
                    .setImgUrl(fillUpNovel.getImgUrl()));
            log.info("补充小说信息{}", WrapMapper.isOk(updateWrapper) ? "成功" : "失败");
        } else {
            log.info("补充小说信息失败");
        }
        model.addAttribute("checkingNovelStart", true);
    }

    private void resetSeqNovelChapter(Model model, Novel novel) {
        model.addAttribute("checkingNovelStart", true);
        Wrapper<List<NovelChapter>> catalogueWrapper = novelLocalService.findCatalogue(novel.getId(), false);
        if (WrapMapper.isFailure(catalogueWrapper)) {
            return;
        }
        List<NovelChapter> chapterList = catalogueWrapper.data();
        for (int i = 0; i < chapterList.size(); i++) {
            chapterList.set(i, chapterList.get(i).setIndex(i));
        }
        Wrapper<Void> deleteCatalogueWrapper = novelLocalService.deleteCatalogue(novel.getId());
        if (WrapMapper.isFailure(deleteCatalogueWrapper)) {
            log.error("删除目录失败");
            return;
        }
        log.info("删除目录成功");
        log.info("插入目录{}", WrapMapper.isOk(novelLocalService.insertCatalogue(chapterList)) ? "成功" : "失败");
        log.info("删除阅读历史记录{}",
                WrapMapper.isOk(novelLocalService.deleteNovelReadHistory(novel.getId())) ? "成功" : "失败");
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
            Wrapper<String> defaultImgUrlWrapper = novelService.findDefaultImgUrl();
            model.addAttribute("defaultImgUrl",
                    WrapMapper.isOk(defaultImgUrlWrapper) ? defaultImgUrlWrapper.data() : null);
        }
        return "novel/local/local_novel_delete";
    }

    private Novel findNovelById(String novelId) {
        Wrapper<List<Novel>> novelWrapper = novelLocalService.findNovel(novelId, null);
        if (WrapMapper.isFailure(novelWrapper) || novelWrapper.data().isEmpty()) {
            return null;
        }
        return novelWrapper.data().getFirst();
    }

    @RequestMapping("action_delete_novel")
    @ViewLog
    public String actionAddVideo(Model model, @RequestParam("novelId") String novelId) {
        Novel novel = findNovelById(novelId);
        if (novel == null) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        Wrapper<Void> deleteNovelWrapper = novelLocalService.deleteNovel(novel);
        model.addAttribute("actionMsg", "删除小说" +
                (WrapMapper.isOk(deleteNovelWrapper) ? "成功" : "失败"));
        model.addAttribute("backUrl", "/novel/local");
        return "action_result";
    }

}
