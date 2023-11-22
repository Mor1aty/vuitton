package com.moriaty.vuitton.service.novel.downloader;

import com.moriaty.vuitton.bean.novel.NovelChapter;
import com.moriaty.vuitton.bean.novel.NovelContent;
import com.moriaty.vuitton.bean.novel.QueryNovelInfo;
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
 * 笔趣阁 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:48
 */
@Component
@Slf4j
public class BiQuGeNovelDownloader implements NovelDownloader {

    @Override
    public NovelDownloaderInfo getInfo() {
        return new NovelDownloaderInfo()
                .setWebName("笔趣阁")
                .setMark("BiQuGe")
                .setWebsite("http://www.bequgew.info")
                .setContentBaseUrl("https://www.bequgew.net/")
                .setCatalogueBaseUrl("http://www.bequgew.info/");
    }

    @Override
    public QueryNovelInfo queryNovel(String queryName) {
        return new QueryNovelInfo()
                .setSourceName("笔趣阁")
                .setSourceWebsite(getInfo().getWebsite())
                .setSourceMark(getInfo().getMark())
                .setWebSearch(getInfo().getWebsite() + "/modules/article/search.php?searchkey=" + queryName);
    }

    @Override
    public List<NovelChapter> findChapterList(String catalogueAppend) {
        try {
            List<NovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(getInfo().getCatalogueBaseUrl() + catalogueAppend).timeout(5000).get();
            Element list = doc.getElementsByClass("article_texttitleb").get(0);
            Elements liList = list.getElementsByTag("li");
            for (int i = 0; i < liList.size(); i++) {
                Element li = liList.get(i);
                Element a = li.getElementsByTag("a").get(0);
                String href = a.attr("href");
                chapterList.add(new NovelChapter()
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
    public NovelContent findContent(String chapterName, String contentAppend) {
        return null;
    }

    @Override
    public String handleContent(String content) {
        return null;
    }
}
