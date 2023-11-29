package com.moriaty.vuitton.service.novel.downloader;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.constant.Constant;
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
 * 2 笔趣阁 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/11 13:58
 */
@Component
@Slf4j
public class TwoBiQuNovelDownloader implements NovelDownloader {

    private final NovelDownloaderInfo info = new NovelDownloaderInfo()
            .setWebName("2笔趣阁")
            .setMark("2BiQu")
            .setWebsite("https://www.2biqu.com")
            .setContentBaseUrl("https://www.2biqu.com/")
            .setCatalogueBaseUrl("https://www.2biqu.com/");

    @Override
    public NovelDownloaderInfo getInfo() {
        return info;
    }

    @Override
    public QueryNetworkNovelInfo queryNovel(String searchText) {
        return new QueryNetworkNovelInfo()
                .setSourceName("2笔趣阁")
                .setSourceWebsite(info.getWebsite())
                .setDownloaderMark(info.getMark())
                .setWebSearch(info.getWebsite() + "/s?q=" + searchText);
    }

    @Override
    public List<NetworkNovelChapter> findChapterList(String catalogueAppend) {
        try {
            List<NetworkNovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(info.getCatalogueBaseUrl() + catalogueAppend)
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .get();
            Element list = doc.getElementById("section-list");
            if (list == null) {
                log.error("find chapter list page error, section-list is not found");
                return Collections.emptyList();
            }
            Elements liList = list.getElementsByTag("li");
            for (int i = 0; i < liList.size(); i++) {
                Element li = liList.get(i);
                Element a = li.getElementsByTag("a").get(0);
                String href = a.attr("href");
                chapterList.add(new NetworkNovelChapter()
                        .setIndex(i)
                        .setName(a.text())
                        .setUrl(catalogueAppend + "/" + href));
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
            log.info("下载 {} {}", chapterName, info.getContentBaseUrl() + contentAppend);
            Document doc = Jsoup.connect(info.getContentBaseUrl() + contentAppend)
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .get();
            Element content = doc.getElementById("content");
            if (content == null) {
                return new NetworkNovelContent()
                        .setErrorMsg("正文不存在");
            }
            return new NetworkNovelContent()
                    .setTitle(chapterName)
                    .setContent(handleContent(content.text()))
                    .setContentHtml(content.html());
        } catch (IOException e) {
            log.error("find content exception occur", e);
            return new NetworkNovelContent()
                    .setErrorMsg("获取小说内容发生异常, " + e.getLocalizedMessage());
        }
    }

    @Override
    public String handleContent(String content) {
        return content.replace("www.2biqu.com 笔趣阁", "");
    }
}
