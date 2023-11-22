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
 * 2 笔趣阁 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/11 13:58
 */
@Component
@Slf4j
public class TwoBiQuNovelDownloader implements NovelDownloader {

    @Override
    public NovelDownloaderInfo getInfo() {
        return new NovelDownloaderInfo()
                .setWebName("2笔趣阁")
                .setMark("2BiQu")
                .setWebsite("https://www.2biqu.com")
                .setContentBaseUrl("https://www.2biqu.com/")
                .setCatalogueBaseUrl("https://www.2biqu.com/");
    }

    @Override
    public QueryNovelInfo queryNovel(String queryName) {
        return new QueryNovelInfo()
                .setSourceName("2笔趣阁")
                .setSourceWebsite(getInfo().getWebsite())
                .setSourceMark(getInfo().getMark())
                .setWebSearch(getInfo().getWebsite() + "/s?q=" + queryName);
    }

    @Override
    public List<NovelChapter> findChapterList(String catalogueAppend) {
        try {
            List<NovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(getInfo().getCatalogueBaseUrl() + catalogueAppend).timeout(5000).get();
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
                chapterList.add(new NovelChapter()
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
    public NovelContent findContent(String chapterName, String contentAppend) {
        try {
            log.info("下载 {} {}", chapterName, getInfo().getContentBaseUrl() + contentAppend);
            Document doc = Jsoup.connect(getInfo().getContentBaseUrl() + contentAppend).timeout(300000)
                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36").get();
            Element content = doc.getElementById("content");
            if (content == null) {
                return new NovelContent()
                        .setErrorMsg("正文不存在");
            }
            return new NovelContent()
                    .setTitle(chapterName)
                    .setContent(handleContent(content.text()))
                    .setContentHtml(content.html());
        } catch (IOException e) {
            log.error("find content exception occur", e);
            return new NovelContent()
                    .setErrorMsg("获取小说内容发生异常, " + e.getLocalizedMessage());
        }
    }

    @Override
    public String handleContent(String content) {
        return content.replace("www.2biqu.com 笔趣阁", "");
    }
}
