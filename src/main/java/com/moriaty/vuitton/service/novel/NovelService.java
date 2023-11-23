package com.moriaty.vuitton.service.novel;

import com.moriaty.vuitton.bean.novel.*;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * 小说 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:23
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Module(id = 0, name = "小说", path = "static/novel")
public class NovelService {

    @Value("${novel.location}")
    private String novelFileLocation;

    public Wrapper<List<QueryNovelInfo>> queryNovel(QueryNovelReq req) {
        if (!StringUtils.hasText(req.getQueryName())) {
            return WrapMapper.illegalParam("小说查询不能为空");
        }
        List<NovelDownloader> downloaderList = NovelFactory.getAllDownloader();
        List<QueryNovelInfo> result = new ArrayList<>();
        for (NovelDownloader downloader : downloaderList) {
            result.add(downloader.queryNovel(req.getQueryName()));
        }
        return WrapMapper.ok(result);
    }


    public Wrapper<String> downloadNovel(DownloadNovelReq req) {
        if (!StringUtils.hasText(req.getDownloaderMark())) {
            return WrapMapper.illegalParam("下载 mark 不能为空");
        }
        if (!StringUtils.hasText(req.getCatalogueAppend())) {
            return WrapMapper.illegalParam("目录补充不能为空");
        }
        if (!StringUtils.hasText(req.getNovelName())) {
            return WrapMapper.illegalParam("小说名不能为空");
        }
        NovelDownloader downloader = NovelFactory.getDownloader(req.getDownloaderMark());
        if (downloader == null) {
            return WrapMapper.failure("小说下载器 " + req.getDownloaderMark() + " 不存在");
        }
        log.info("开始下载小说 {} [{}-{}-{}]", req.getNovelName(), downloader.getInfo().getWebName(),
                downloader.getInfo().getMark(), downloader.getInfo().getWebsite());
        List<NovelChapter> chapterList = downloader.findChapterList(req.getCatalogueAppend());
        String file = novelFileLocation + File.separator + req.getNovelName() + ".txt";

        try {
            // 使用虚拟线程下载
            Map<Integer, NovelContent> novelMap = HashMap.newHashMap(chapterList.size());
            Map<Integer, NovelContent> errorNovelChapter = HashMap.newHashMap(0);
            ThreadFactory factory = Thread.ofVirtual().name("novel-downloader", 0).factory();
            CountDownLatch countDownLatch = new CountDownLatch(chapterList.size());
            log.info("共 {} 章, 开启虚拟线程: {}", countDownLatch.getCount(), countDownLatch.getCount());
            for (int i = 0; i < chapterList.size(); i++) {
                int index = i;
                int sleepSecond = i % 10;
                factory.newThread(() -> {
                    try {
                        Thread.sleep(Duration.ofSeconds(sleepSecond));
                    } catch (InterruptedException e) {
                        log.error("sleep is interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                    NovelChapter chapter = chapterList.get(index);
                    NovelContent content = downloader.findContent(chapter.getName(), chapter.getUrl());
                    novelMap.put(chapter.getIndex(), content);
                    if (StringUtils.hasText(content.getErrorMsg())) {
                        errorNovelChapter.put(chapter.getIndex(), content);
                    }
                    countDownLatch.countDown();
                }).start();
            }
            countDownLatch.await();
            log.info("错误章节: {}", errorNovelChapter.size());
            log.info("章节: {}", novelMap.size());
            handleDownloadResult(chapterList, novelMap, errorNovelChapter);
            if (writeNovelToFile(req.getNovelName(), chapterList.size(), novelMap)) {
                return WrapMapper.ok(file);
            } else {
                return WrapMapper.failure("小说写入文件失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return WrapMapper.failure("下载小说时被打断");
        }
    }

    private void handleDownloadResult(List<NovelChapter> chapterList, Map<Integer, NovelContent> novelMap,
                                      Map<Integer, NovelContent> errorNovelChapter) {
        if (chapterList.size() <= novelMap.size() + errorNovelChapter.size()) {
            return;
        }
        log.info("下载存在异常章节");
        for (NovelChapter chapter : chapterList) {
            if (!novelMap.containsKey(chapter.getIndex()) && !errorNovelChapter.containsKey(chapter.getIndex())) {
                log.info("章节 {}-{}-{} 并未下载", chapter.getIndex(), chapter.getName(), chapter.getUrl());
            }
        }
    }

    private boolean writeNovelToFile(String novelName, int chapterNum, Map<Integer, NovelContent> novelMap) {
        String file = novelFileLocation + File.separator + novelName + ".txt";
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(novelName);
            fileWriter.write("\n\n");
            for (int i = 0; i < chapterNum; i++) {
                NovelContent content = novelMap.get(i);
                int index = i + 1;
                fileWriter.write("第" + index + "折 ");
                if (content == null) {
                    fileWriter.write("本章不存在");
                    fileWriter.write("\n\n");
                } else if (StringUtils.hasText(content.getErrorMsg())) {
                    fileWriter.write("异常章节");
                    fileWriter.write("\n\n");
                    fileWriter.write(content.getErrorMsg());
                    fileWriter.write("\n\n");
                } else {
                    fileWriter.write(content.getTitle());
                    fileWriter.write("\n\n");
                    fileWriter.write(content.getContent());
                    fileWriter.write("\n\n");
                }

            }
            log.info("{} 写入完成", novelName);
            return true;
        } catch (IOException e) {
            log.error("小说写入文件异常", e);
            return false;
        }
    }

    public Wrapper<List<NovelChapter>> findCatalogue(FindCatalogueReq req) {
        if (!StringUtils.hasText(req.getDownloaderMark())) {
            return WrapMapper.illegalParam("下载 mark 不能为空");
        }
        if (!StringUtils.hasText(req.getCatalogueAppend())) {
            return WrapMapper.illegalParam("目录补充不能为空");
        }
        NovelDownloader downloader = NovelFactory.getDownloader(req.getDownloaderMark());
        if (downloader == null) {
            return WrapMapper.failure("小说下载器 " + req.getDownloaderMark() + " 不存在");
        }
        List<NovelChapter> chapterList = downloader.findChapterList(req.getCatalogueAppend());
        return WrapMapper.ok(chapterList);
    }

    public Wrapper<NovelContent> findContent(FindContentReq req) {
        if (!StringUtils.hasText(req.getDownloaderMark())) {
            return WrapMapper.illegalParam("下载 mark 不能为空");
        }
        if (!StringUtils.hasText(req.getChapterName())) {
            return WrapMapper.illegalParam("章节名不能为空");
        }
        if (!StringUtils.hasText(req.getChapterUrl())) {
            return WrapMapper.illegalParam("章节 url 不能为空");
        }
        NovelDownloader downloader = NovelFactory.getDownloader(req.getDownloaderMark());
        if (downloader == null) {
            return WrapMapper.failure("小说下载器 " + req.getDownloaderMark() + " 不存在");
        }
        NovelContent content = downloader.findContent(req.getChapterName(), req.getChapterUrl());
        content.setTitle(req.getChapterName());
        return WrapMapper.ok(content);
    }
}
