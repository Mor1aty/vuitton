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
 * 全民小说 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:48
 */
@Component
@Slf4j
public class QuanMingNovelDownloader implements NovelDownloader {

    @Override
    public NovelDownloaderInfo getInfo() {
        return new NovelDownloaderInfo()
                .setWebName("全民小说")
                .setMark("QuanMing")
                .setWebsite("https://www.1q1m.com")
                .setContentBaseUrl("https://www.1q1m.com/")
                .setCatalogueBaseUrl("https://www.1q1m.com/book/");
    }

    @Override
    public QueryNovelInfo queryNovel(String queryName) {
        return new QueryNovelInfo()
                .setSourceName("全民小说")
                .setSourceWebsite(getInfo().getWebsite())
                .setSourceMark(getInfo().getMark())
                .setWebSearch(getInfo().getWebsite() + "/s?q=" + queryName);
    }

    @Override
    public List<NovelChapter> findChapterList(String catalogueAppend) {
        try {
            List<NovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(getInfo().getCatalogueBaseUrl() + catalogueAppend).timeout(5000).get();
            Element list = doc.getElementsByClass("listmain").get(0);
            Elements ddList = list.getElementsByTag("dd");
            for (int i = 0; i < ddList.size(); i++) {
                Element dd = ddList.get(i);
                if (dd.hasClass("pc_none")) {
                    continue;
                }
                Element a = dd.getElementsByTag("a").get(0);
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
        try {
            Document doc = Jsoup.connect(getInfo().getContentBaseUrl() + contentAppend).timeout(300000)
                    .ignoreHttpErrors(true).get();
            Element content = doc.getElementById("chaptercontent");
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
        String space = "\s\s\s\s";
        return content
                .replace("请收藏本站：https://www.1q1m.com。全民小说网手机版：https://m.1q1m.com 『点此报错』『加入书签』",
                        "")
                .replace(space, "\n");

    }
}
