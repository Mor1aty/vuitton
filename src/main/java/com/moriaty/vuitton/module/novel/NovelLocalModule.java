package com.moriaty.vuitton.module.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.bean.novel.local.NovelReadHistoryInfo;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.entity.NovelReadHistory;
import com.moriaty.vuitton.dao.mapper.NovelChapterMapper;
import com.moriaty.vuitton.dao.mapper.NovelMapper;
import com.moriaty.vuitton.dao.mapper.NovelReadHistoryMapper;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloaderFactory;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 本地小说模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/18 17:17
 */
@Service("novelLocalModule")
@RequiredArgsConstructor
@Slf4j
public class NovelLocalModule implements InitializingBean {

    private final NovelMapper novelMapper;

    private final NovelChapterMapper novelChapterMapper;

    private final NovelReadHistoryMapper novelReadHistoryMapper;

    @Value("${file-server.novel.location}")
    private String fsNovelFileLocation;

    @Value("${file-server.novel.module-local-img}")
    private String fsNovelModuleLocalImg;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(1)
                .setName("本地小说")
                .setPath("novel/local")
                .setImgUrl(fsNovelModuleLocalImg));
    }

    public List<Novel> findNovel(String id, String name) {
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(id)) {
            queryWrapper.eq(Novel::getId, id);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Novel::getName, name);
        }
        return novelMapper.selectList(queryWrapper);
    }

    public List<NovelChapter> findCatalogue(String novelId, boolean reverse) {
        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId);
        if (reverse) {
            queryWrapper.orderByDesc(NovelChapter::getIndex);
        } else {
            queryWrapper.orderByAsc(NovelChapter::getIndex);
        }
        return novelChapterMapper.selectList(queryWrapper);
    }

    public void insertCatalogue(List<NovelChapter> chapterList) {
        novelChapterMapper.insertBatch(chapterList);
    }

    public void deleteCatalogue(String novelId) {
        novelChapterMapper.delete(new LambdaQueryWrapper<NovelChapter>().eq(NovelChapter::getNovel, novelId));
    }

    public LocalNovelAroundChapter findAroundChapter(String novelId, int chapterIndex) {
        LocalNovelAroundChapter aroundChapter = new LocalNovelAroundChapter();
        List<NovelChapter> chapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .eq(NovelChapter::getIndex, chapterIndex));
        if (chapterList == null || chapterList.isEmpty()) {
            return null;
        }
        aroundChapter.setChapter(chapterList.getFirst());
        List<NovelChapter> lessChapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .lt(NovelChapter::getIndex, chapterIndex)
                .orderByDesc(NovelChapter::getIndex));
        if (lessChapterList != null && !lessChapterList.isEmpty()) {
            aroundChapter.setPreChapter(lessChapterList.getFirst());
        }
        List<NovelChapter> greatChapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .gt(NovelChapter::getIndex, chapterIndex)
                .orderByAsc(NovelChapter::getIndex));
        if (greatChapterList != null && !greatChapterList.isEmpty()) {
            aroundChapter.setNextChapter(greatChapterList.getFirst());
        }
        return aroundChapter;
    }

    public List<NovelReadHistoryInfo> findNovelReadHistory(String novelId) {
        return novelReadHistoryMapper.findNovelReadHistory(novelId);
    }

    public void insertNovelReadHistory(String novelId, int chapterIndex) {
        novelReadHistoryMapper.delete(new LambdaQueryWrapper<NovelReadHistory>()
                .eq(NovelReadHistory::getNovel, novelId));
        novelReadHistoryMapper.insert(new NovelReadHistory()
                .setId(UuidUtil.genId())
                .setReadTime(LocalDateTime.now().format(Constant.Date.FORMAT_RECORD_TIME))
                .setNovel(novelId)
                .setChapterIndex(chapterIndex)
        );
    }

    public void deleteNovelReadHistory(String novelId) {
        novelReadHistoryMapper.delete(new LambdaQueryWrapper<NovelReadHistory>()
                .eq(NovelReadHistory::getNovel, novelId));
    }

    public boolean recoverNovelChapter(String novelId, int chapterIndex) {
        try {
            List<Novel> novelList = novelMapper.selectList(new LambdaQueryWrapper<Novel>()
                    .eq(Novel::getId, novelId));
            if (novelList == null || novelList.isEmpty()) {
                return false;
            }
            Novel novel = novelList.getFirst();
            log.info("查询小说[{} {}]章节, 来源: {}", novel.getName(), novel.getDownloaderCatalogueUrl(),
                    novel.getDownloaderMark());
            NovelDownloader downloader = NovelDownloaderFactory.getDownloader(novel.getDownloaderMark());
            if (downloader == null) {
                return false;
            }
            List<NetworkNovelChapter> chapterList = downloader.findChapterList(novel.getDownloaderCatalogueUrl());
            if (chapterList.isEmpty() || chapterList.size() <= chapterIndex) {
                return false;
            }
            NetworkNovelChapter networkNovelChapter = chapterList.get(chapterIndex);
            if (networkNovelChapter.getIndex() != chapterIndex) {
                return false;
            }
            log.info("查询小说[{}]章节[{}]内容, 来源: {}", novel.getName(), networkNovelChapter.getName(),
                    novel.getDownloaderMark());
            NetworkNovelContent content = downloader.findContent(networkNovelChapter.getName(),
                    novel.getDownloaderCatalogueUrl());
            if (content == null) {
                return false;
            }
            novelChapterMapper.insert(new NovelChapter()
                    .setId(UuidUtil.genId())
                    .setNovel(novelId)
                    .setIndex(chapterIndex)
                    .setName(networkNovelChapter.getName())
                    .setContent(content.getContent())
                    .setContentHtml(content.getContentHtml()));
            String filePath = writeNovelToFile(novel);
            if (filePath != null) {
                novelMapper.updateById(new Novel().setId(novelId).setFilePath(filePath));
                Files.deleteIfExists(Paths.get(novel.getFilePath()));
            }
            return true;
        } catch (IOException e) {
            log.error("恢复小说文件发生异常", e);
            return false;
        }
    }

    private String writeNovelToFile(Novel novel) {
        String file = fsNovelFileLocation + File.separator + novel.getName() + "-" + UuidUtil.genId() + ".txt";
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            List<NovelChapter> chapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                    .eq(NovelChapter::getNovel, novel.getId()).orderByAsc(NovelChapter::getIndex));
            for (int i = 0; i < chapterList.size(); i++) {
                int index = i + 1;
                NovelChapter chapter = chapterList.get(i);
                fileWriter.write("第" + index + "折 ");
                if (chapter.getContent() == null) {
                    fileWriter.write("本章不存在");
                    fileWriter.write("\n\n");
                } else {
                    fileWriter.write(chapter.getName());
                    fileWriter.write("\n\n");
                    fileWriter.write(chapter.getContent());
                    fileWriter.write("\n\n");
                }
            }
            return file;
        } catch (IOException e) {
            log.error("小说写入文件异常", e);
            return null;
        }
    }

    public boolean deleteNovel(Novel novel) {
        novelChapterMapper.delete(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novel.getId()));
        novelMapper.delete(new LambdaQueryWrapper<Novel>()
                .eq(Novel::getId, novel.getId()));
        try {
            Files.deleteIfExists(Paths.get(novel.getFilePath()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void updateNovel(Novel novel) {
        novelMapper.updateById(novel);
    }
}
