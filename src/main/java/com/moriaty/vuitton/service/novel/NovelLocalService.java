package com.moriaty.vuitton.service.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.bean.novel.local.NovelReadHistoryInfo;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.entity.NovelReadHistory;
import com.moriaty.vuitton.dao.mapper.NovelChapterMapper;
import com.moriaty.vuitton.dao.mapper.NovelMapper;
import com.moriaty.vuitton.dao.mapper.NovelReadHistoryMapper;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 本地小说 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 下午2:34
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelLocalService {

    private final NovelMapper novelMapper;

    private final NovelChapterMapper novelChapterMapper;

    private final NovelReadHistoryMapper novelReadHistoryMapper;

    @Value("${file-server.novel.location}")
    private String fsNovelFileLocation;

    public Wrapper<List<Novel>> findNovel(String id, String name) {
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(id)) {
            queryWrapper.eq(Novel::getId, id);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Novel::getName, name);
        }
        List<Novel> novelList = novelMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", novelList);
    }

    public Wrapper<List<NovelChapter>> findCatalogue(String novelId, boolean reverse) {
        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId);
        if (reverse) {
            queryWrapper.orderByDesc(NovelChapter::getIndex);
        } else {
            queryWrapper.orderByAsc(NovelChapter::getIndex);
        }
        List<NovelChapter> chapterList = novelChapterMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", chapterList);
    }

    public Wrapper<LocalNovelAroundChapter> findAroundChapter(String novelId, int chapterIndex) {
        LocalNovelAroundChapter aroundChapter = new LocalNovelAroundChapter();
        List<NovelChapter> chapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .eq(NovelChapter::getIndex, chapterIndex));
        if (chapterList == null || chapterList.isEmpty()) {
            return WrapMapper.failure("小说章节不存在");
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
        return WrapMapper.ok("获取成功", aroundChapter);
    }

    public Wrapper<List<NovelReadHistoryInfo>> findNovelReadHistory(String novelId) {
        List<NovelReadHistoryInfo> novelReadHistory = novelReadHistoryMapper.findNovelReadHistory(novelId);
        return WrapMapper.ok("获取成功", novelReadHistory);
    }

    public Wrapper<Void> insertNovelReadHistory(String novelId, int chapterIndex) {
        novelReadHistoryMapper.delete(new LambdaQueryWrapper<NovelReadHistory>()
                .eq(NovelReadHistory::getId, novelId));
        novelReadHistoryMapper.insert(new NovelReadHistory()
                .setId(UuidUtil.genId())
                .setReadTime(LocalDateTime.now().format(Constant.Date.FORMAT_RECORD_TIME))
                .setNovel(novelId)
                .setChapterIndex(chapterIndex)
        );
        return WrapMapper.ok("插入成功");
    }

    public Wrapper<Void> recoverNovelChapter(String novelId, int chapterIndex) {
        List<Novel> novelList = novelMapper.selectList(new LambdaQueryWrapper<Novel>()
                .eq(Novel::getId, novelId));
        if (novelList == null || novelList.isEmpty()) {
            return WrapMapper.failure("小说不存在");
        }
        Novel novel = novelList.getFirst();
        log.info("查询小说[{} {}]章节, 来源: {}", novel.getName(), novel.getDownloaderCatalogueUrl(),
                novel.getDownloaderMark());
        NovelDownloader downloader = NovelFactory.getDownloader(novel.getDownloaderMark());
        if (downloader == null) {
            return WrapMapper.failure("小说下载器 " + novel.getDownloaderMark() + " 不存在");
        }
        List<NetworkNovelChapter> chapterList = downloader.findChapterList(novel.getDownloaderCatalogueUrl());
        if (chapterList.isEmpty() || chapterList.size() <= chapterIndex) {
            return WrapMapper.failure("恢复的章节不存在");
        }
        NetworkNovelChapter networkNovelChapter = chapterList.get(chapterIndex);
        if (networkNovelChapter.getIndex() != chapterIndex) {
            return WrapMapper.failure("恢复的章节 index 不存在");
        }
        log.info("查询小说[{}]章节[{}]内容, 来源: {}", novel.getName(), networkNovelChapter.getName(),
                novel.getDownloaderMark());
        NetworkNovelContent content = downloader.findContent(networkNovelChapter.getName(),
                novel.getDownloaderCatalogueUrl());
        if (content == null) {
            return WrapMapper.failure("恢复的章节内容不存在");
        }
        try {
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
            return WrapMapper.ok("恢复成功");
        } catch (IOException e) {
            log.error("恢复小说文件发生异常", e);
            return WrapMapper.failure("恢复小说文件发生异常");
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

    public Wrapper<Void> deleteNovel(Novel novel) {
        novelChapterMapper.delete(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novel.getId()));
        novelMapper.delete(new LambdaQueryWrapper<Novel>()
                .eq(Novel::getId, novel.getId()));
        try {
            Files.deleteIfExists(Paths.get(novel.getFilePath()));
            return WrapMapper.ok("删除成功");
        } catch (IOException e) {
            return WrapMapper.failure("删除小说文件失败");
        }
    }

    public Wrapper<Void> updateNovel(Novel novel) {
        novelMapper.updateById(novel);
        return WrapMapper.ok("更新成功");
    }
}
