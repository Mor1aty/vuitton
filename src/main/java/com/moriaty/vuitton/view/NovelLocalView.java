package com.moriaty.vuitton.view;

import com.moriaty.vuitton.bean.KeyValuePair;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
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
        return "novel/local/local_novel_content";
    }

}
