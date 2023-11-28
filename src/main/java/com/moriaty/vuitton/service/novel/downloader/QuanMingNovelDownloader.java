package com.moriaty.vuitton.service.novel.downloader;


import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.util.HtmlUnitUtil;
import com.moriaty.vuitton.util.UuidUtil;
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

    private final NovelDownloaderInfo info = new NovelDownloaderInfo()
            .setWebName("全民小说")
            .setMark("QuanMing")
            .setWebsite("https://www.1q1m.com")
            .setContentBaseUrl("https://www.1q1m.com/")
            .setCatalogueBaseUrl("https://www.1q1m.com/book/");

    @Override
    public NovelDownloaderInfo getInfo() {
        return info;
    }

    @Override
    public QueryNetworkNovelInfo queryNovel(String searchText) {
        try {
            HtmlPage page = HtmlUnitUtil.getPage(info.getWebsite() + "/s?q=" + searchText);
            List<HtmlElement> domList = page.getByXPath("//div[@class='box']");
            List<NetworkNovelInfo> novelInfoList = new ArrayList<>();
            for (HtmlElement dom : domList) {
                NetworkNovelInfo novelInfo = findNovelInfoFromDom(dom);
                if (novelInfo != null) {
                    novelInfoList.add(novelInfo
                            .setStorageKey(UuidUtil.genId()));
                }
            }
            return new QueryNetworkNovelInfo()
                    .setSourceName("全民小说")
                    .setSourceWebsite(info.getWebsite())
                    .setSourceMark(info.getMark())
                    .setWebSearch(info.getWebsite() + "/s?q=" + searchText)
                    .setNovelInfoList(novelInfoList);
        } catch (IOException e) {
            log.error("查询发生异常", e);
            return new QueryNetworkNovelInfo()
                    .setErrorMsg("查询发生异常");
        }
    }

    private NetworkNovelInfo findNovelInfoFromDom(HtmlElement dom) {
        NetworkNovelInfo novelInfo = new NetworkNovelInfo();
        DomNodeList<HtmlElement> divList = dom.getElementsByTagName("div");
        int divNum = 4;
        if (divList.size() != divNum) {
            return null;
        }
        HtmlElement imgDiv = divList.getFirst();
        DomNodeList<HtmlElement> img = imgDiv.getElementsByTagName("img");
        if (!img.isEmpty()) {
            novelInfo.setImgUrl(img.getFirst().getAttribute("src"));
        }
        DomNodeList<HtmlElement> h4 = dom.getElementsByTagName("h4");
        if (!h4.isEmpty()) {
            DomNodeList<HtmlElement> a = h4.getFirst().getElementsByTagName("a");
            if (!a.isEmpty()) {
                HtmlElement aFirst = a.getFirst();
                novelInfo.setName(aFirst.getTextContent());
                novelInfo.setChapterUrl(aFirst.getAttribute("href"));
            }
        }
        HtmlElement author = divList.get(2);
        String[] nameSplit = author.getTextContent().split("作者：");
        int splitLength = 2;
        if (nameSplit.length == splitLength) {
            novelInfo.setAuthor(nameSplit[1]);
        } else {
            novelInfo.setAuthor(nameSplit[0]);
        }
        HtmlElement intro = divList.get(3);
        novelInfo.setIntro(intro.getTextContent());
        return novelInfo;
    }

    @Override
    public List<NetworkNovelChapter> findChapterList(String catalogueAppend) {
        try {
            List<NetworkNovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(info.getCatalogueBaseUrl() + catalogueAppend).timeout(5000).get();
            Element list = doc.getElementsByClass("listmain").get(0);
            Elements ddList = list.getElementsByTag("dd");
            for (int i = 0; i < ddList.size(); i++) {
                Element dd = ddList.get(i);
                if (dd.hasClass("pc_none")) {
                    continue;
                }
                Element a = dd.getElementsByTag("a").get(0);
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
            Document doc = Jsoup.connect(info.getContentBaseUrl() + contentAppend).timeout(300000)
                    .ignoreHttpErrors(true).get();
            Element content = doc.getElementById("chaptercontent");
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
        String space = "\s\s\s\s";
        return content
                .replace("请收藏本站：https://www.1q1m.com。全民小说网手机版：https://m.1q1m.com 『点此报错』『加入书签』",
                        "")
                .replace(space, "\n");

    }
}
