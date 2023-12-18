package com.moriaty.vuitton.module.novel.downloader.instance;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloaderInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 笔翠小说 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/9 3:30
 */
@Component
@Slf4j
public class BiCuiNovelDownloader implements NovelDownloader {

    private final NovelDownloaderInfo info = new NovelDownloaderInfo()
            .setWebName("笔翠小说")
            .setMark("BiCui")
            .setWebsite("https://www.bicuix.com")
            .setContentBaseUrl("https://www.bicuix.com/")
            .setCatalogueBaseUrl("https://www.bicuix.com/")
            .setSearchBaseUrl("");

    @Override
    public NovelDownloaderInfo getInfo() {
        return info;
    }

    @Override
    public QueryNetworkNovelInfo queryNovel(String searchText) {
        return null;
    }

    @Override
    public List<NetworkNovelChapter> findChapterList(String catalogueUrl) {
        try {
            List<NetworkNovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(info.getCatalogueBaseUrl() + catalogueUrl)
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .get();
            Element list = doc.getElementById("list");
            Element dl = list.getElementsByTag("dl").getFirst();
            Elements beforeDdList = dl.getElementsByTag("dd");
            List<Element> afterDdList = beforeDdList.subList(9, beforeDdList.size());
            log.info("afterDdList: {}", afterDdList.size());
            for (int i = 0; i < afterDdList.size(); i++) {
                Element dd = afterDdList.get(i);
                Element a = dd.getElementsByTag("a").getFirst();
                String href = a.attr("href");
                chapterList.add(new NetworkNovelChapter()
                        .setIndex(i)
                        .setName(a.text())
                        .setUrl(href));
            }
            return chapterList;
        } catch (IOException e) {
            log.error("find chapter list exception occur", e);
            return Collections.emptyList();
        }
    }

    @Override
    public NetworkNovelContent findContent(String chapterName, String contentAppend) {
        try {
            Document doc = Jsoup.connect(info.getContentBaseUrl() + contentAppend)
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .ignoreHttpErrors(true)
                    .get();
            Element content = doc.getElementById("content");
            if (content == null) {
                return new NetworkNovelContent()
                        .setErrorMsg("正文不存在");
            }
            return new NetworkNovelContent()
                    .setTitle(chapterName)
                    .setContent(handleContent(content.html()))
                    .setContentHtml(handleContent(content.html()));
        } catch (IOException e) {
            return new NetworkNovelContent()
                    .setErrorMsg("获取小说内容发生异常, " + e.getLocalizedMessage());
        }
    }

    @Override
    public String handleContent(String content) {
        return content.replace("<br>", "\n").replace("&nbsp;", " ");
    }
}
