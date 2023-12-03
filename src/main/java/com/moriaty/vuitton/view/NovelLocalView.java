package com.moriaty.vuitton.view;

import com.alibaba.fastjson2.TypeReference;
import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.bean.novel.local.NovelCheckChapter;
import com.moriaty.vuitton.bean.novel.local.NovelReadHistoryInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.service.novel.NovelLocalService;
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
        return "novel/local/local_novel";
    }

    @RequestMapping("novel_info")
    @ViewLog
    public String localNovelInfo(Model model, @RequestParam("novelId") String novelId) {
        Wrapper<List<Novel>> novelWrapper = novelLocalService.findNovel(novelId, null);
        if (!WrapMapper.isOk(novelWrapper) || novelWrapper.data().isEmpty()) {
            return ViewUtil.goError(model, "本地小说不存在", KeyValuePair.of("novelId", novelId));
        }

        model.addAttribute("novel", novelWrapper.data().getFirst());
        Wrapper<List<NovelChapter>> catalogueWrapper = novelLocalService.findCatalogue(novelId);
        model.addAttribute("chapterList",
                WrapMapper.isOk(catalogueWrapper) ? catalogueWrapper.data() : Collections.emptyList());
        Wrapper<List<NovelReadHistoryInfo>> readHistoryWrapper = novelLocalService.findNovelReadHistory(novelId);
        model.addAttribute("readHistory",
                !WrapMapper.isOk(readHistoryWrapper) || readHistoryWrapper.data().isEmpty() ?
                        null : readHistoryWrapper.data().getFirst());
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
        if (!WrapMapper.isOk(insertWrapper)) {
            return ViewUtil.goError(model, "插入阅读记录出问题啦", KeyValuePair.ofList(
                    "novelId", novelId, "chapterIndex", chapterIndexStr));
        }
        return "novel/local/local_novel_content";
    }

    @RequestMapping("novel_check")
    @ViewLog
    public String localNovelCheck(Model model, @RequestParam("novelId") String novelId,
                                  @RequestParam("novelCheckAction") String novelCheckActionStr) {

        if (ViewUtil.checkIllegalParam(Collections.emptyList(),
                () -> !novelCheckActionStr.matches(Constant.Regex.NATURE_NUMBER))) {
            return ViewUtil.goParamError(model,
                    KeyValuePair.ofList("novelId", novelId, "novelCheckAction", novelCheckActionStr));
        }
        Wrapper<List<Novel>> novelWrapper = novelLocalService.findNovel(novelId, null);
        if (!WrapMapper.isOk(novelWrapper) || novelWrapper.data().isEmpty()) {
            return ViewUtil.goError(model, "小说不存在", KeyValuePair.of("novelId", novelId));
        }
        model.addAttribute("novel", novelWrapper.data().getFirst());

        int novelCheckAction = Integer.parseInt(novelCheckActionStr);
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_ASK) {
            model.addAttribute("lossNovelChapterList", checkNovelChapter(novelId));
        }
        if (novelCheckAction == Constant.Novel.CHECK_ACTION_DO) {
            List<Integer> checkingChapterIndexList = checkNovelChapter(novelId).stream()
                    .map(NovelCheckChapter::getChapterIndex).toList();
            if (!checkingChapterIndexList.isEmpty()) {
                checkingChapterIndexList.forEach(checkingChapterIndex -> {
                    String itemIndex = MemoryStorage.putList(Constant.Novel.CHECKING_STORAGE_KEY, checkingChapterIndex);
                    Thread.ofVirtual().name("novelCheck-", 0)
                            .start(() -> {
                                Wrapper<Void> recoverWrapper = novelLocalService.recoverNovelChapter(novelId, checkingChapterIndex);
                                log.info("恢复 {} {} {}", novelId, checkingChapterIndex,
                                        WrapMapper.isOk(recoverWrapper) ? "成功" : "失败, " + recoverWrapper.msg());
                                MemoryStorage.removeListItem(Constant.Novel.CHECKING_STORAGE_KEY, itemIndex);
                            });
                });
            }
            model.addAttribute("checkingNovelStart", true);
        }
        List<Integer> checkingChapterList = MemoryStorage.getList(Constant.Novel.CHECKING_STORAGE_KEY,
                new TypeReference<>() {
                });
        model.addAttribute("checkingChapterList", checkingChapterList);

        return "novel/local/local_novel_check";
    }

    private List<NovelCheckChapter> checkNovelChapter(String novelId) {
        Wrapper<List<NovelChapter>> catalogueWrapper = novelLocalService.findCatalogue(novelId);
        if (!WrapMapper.isOk(catalogueWrapper)) {
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
}
