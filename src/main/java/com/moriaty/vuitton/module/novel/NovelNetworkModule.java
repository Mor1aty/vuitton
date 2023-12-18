package com.moriaty.vuitton.module.novel;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.mapper.NovelChapterMapper;
import com.moriaty.vuitton.dao.mapper.NovelMapper;
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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * 网络小说模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/18 20:26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelNetworkModule implements InitializingBean {

    private final NovelMapper novelMapper;

    private final NovelChapterMapper novelChapterMapper;

    @Value("${file-server.novel.location}")
    private String fsNovelFileLocation;

    @Value("${file-server.novel.module-network-img}")
    private String fsNovelModuleNetworkImg;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(2)
                .setName("网络小说")
                .setPath("novel/network")
                .setImgUrl(fsNovelModuleNetworkImg));
    }

    public List<QueryNetworkNovelInfo> queryNovel(String searchText, List<String> downloaderMarkList) {
        if (!StringUtils.hasText(searchText)) {
            return Collections.emptyList();
        }
        log.info("查询小说: {}, 来源: {}", searchText, downloaderMarkList);
        List<NovelDownloader> downloaderList = new ArrayList<>();
        if (downloaderMarkList != null) {
            downloaderMarkList.forEach(downloaderMark
                    -> downloaderList.add(NovelDownloaderFactory.getDownloader(downloaderMark)));
        } else {
            downloaderList.addAll(NovelDownloaderFactory.getAllDownloader());
        }
        List<QueryNetworkNovelInfo> resultList = new ArrayList<>();
        for (NovelDownloader downloader : downloaderList) {
            resultList.add(downloader.queryNovel(searchText));
        }
        MemoryStorage.put("queryNetworkNovel-" + UuidUtil.genId(), resultList);
        return resultList;
    }

    public String downloadNovel(String downloaderMark, NetworkNovelInfo novelInfo, boolean isToDb) {
        if (!StringUtils.hasText(downloaderMark)) {
            return null;
        }
        if (!StringUtils.hasText(novelInfo.getCatalogueUrl())) {
            return null;
        }
        if (!StringUtils.hasText(novelInfo.getName())) {
            return null;
        }
        NovelDownloader downloader = NovelDownloaderFactory.getDownloader(downloaderMark);
        if (downloader == null) {
            return null;
        }
        log.info("开始下载小说 {} [{}-{}-{}]", novelInfo.getName(), downloader.getInfo().getWebName(),
                downloader.getInfo().getMark(), downloader.getInfo().getWebsite());
        List<NetworkNovelChapter> chapterList = downloader.findChapterList(novelInfo.getCatalogueUrl());
        if (chapterList.isEmpty()) {
            return null;
        }
        String file = fsNovelFileLocation + File.separator + novelInfo.getName() + "-" + UuidUtil.genId() + ".txt";
        try {
            // 使用虚拟线程下载
            Map<Integer, NetworkNovelContent> novelMap = HashMap.newHashMap(chapterList.size());
            Map<Integer, NetworkNovelContent> errorNovelChapter = HashMap.newHashMap(0);
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
                    NetworkNovelChapter chapter = chapterList.get(index);
                    NetworkNovelContent content = downloader.findContent(chapter.getName(), chapter.getUrl());
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
            if (writeNovelToFile(novelInfo.getName(), chapterList.size(), novelMap, file)) {
                if (isToDb) {
                    writeNovelToDb(downloaderMark, novelInfo, novelMap, file);
                }
                return file;
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void handleDownloadResult(List<NetworkNovelChapter> chapterList, Map<Integer, NetworkNovelContent> novelMap,
                                      Map<Integer, NetworkNovelContent> errorNovelChapter) {
        if (errorNovelChapter.isEmpty() || chapterList.size() <= novelMap.size() + errorNovelChapter.size()) {
            return;
        }
        log.info("下载存在异常章节");
        for (NetworkNovelChapter chapter : chapterList) {
            if (!novelMap.containsKey(chapter.getIndex()) && !errorNovelChapter.containsKey(chapter.getIndex())) {
                log.info("章节 {}-{}-{} 并未下载", chapter.getIndex(), chapter.getName(), chapter.getUrl());
            }
        }
    }

    private boolean writeNovelToFile(String novelName, int chapterNum,
                                     Map<Integer, NetworkNovelContent> novelMap, String file) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(novelName);
            fileWriter.write("\n\n");
            for (int i = 0; i < chapterNum; i++) {
                NetworkNovelContent content = novelMap.get(i);
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

    private void writeNovelToDb(String downloaderMark, NetworkNovelInfo novelInfo,
                                Map<Integer, NetworkNovelContent> novelMap, String file) {
        String novelId = UuidUtil.genId();
        novelMapper.insert(new Novel()
                .setId(novelId)
                .setName(novelInfo.getName())
                .setAuthor(novelInfo.getAuthor())
                .setIntro(novelInfo.getIntro())
                .setImgUrl(novelInfo.getImgUrl())
                .setFilePath(file)
                .setDownloaderMark(downloaderMark)
                .setDownloaderCatalogueUrl(novelInfo.getCatalogueUrl()));
        novelMap.forEach((index, novel) -> {
            if (StringUtils.hasText(novel.getErrorMsg())) {
                return;
            }
            novelChapterMapper.insert(new NovelChapter()
                    .setId(UuidUtil.genId())
                    .setNovel(novelId)
                    .setName(novel.getTitle())
                    .setIndex(index)
                    .setContent(novel.getContent())
                    .setContentHtml(novel.getContentHtml()));
        });
    }

    public List<NetworkNovelChapter> findCatalogue(String downloaderMark, String catalogueUrl, boolean reverse) {
        List<NetworkNovelChapter> chapterList = new ArrayList<>();
        if (!StringUtils.hasText(downloaderMark)) {
            return chapterList;
        }
        if (!StringUtils.hasText(catalogueUrl)) {
            return chapterList;
        }
        log.info("查询小说[{}]章节, 来源: {}", catalogueUrl, downloaderMark);
        NovelDownloader downloader = NovelDownloaderFactory.getDownloader(downloaderMark);
        if (downloader == null) {
            return chapterList;
        }
        chapterList.addAll(downloader.findChapterList(catalogueUrl));
        if (reverse) {
            chapterList = chapterList.reversed();
        }
        return chapterList;
    }

    public NetworkNovelContent findContent(String chapterName, String chapterUrl, String downloaderMark) {
        if (!StringUtils.hasText(downloaderMark)) {
            return null;
        }
        if (!StringUtils.hasText(chapterName)) {
            return null;
        }
        if (!StringUtils.hasText(chapterUrl)) {
            return null;
        }
        log.info("查询小说章节[{}]内容, 来源: {}", chapterName, downloaderMark);
        NovelDownloader downloader = NovelDownloaderFactory.getDownloader(downloaderMark);
        if (downloader == null) {
            return null;
        }
        NetworkNovelContent content = downloader.findContent(chapterName, chapterUrl);
        content.setTitle(chapterName);
        return content;
    }
}
