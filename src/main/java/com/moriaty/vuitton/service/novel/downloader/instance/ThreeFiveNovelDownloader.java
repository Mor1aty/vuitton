package com.moriaty.vuitton.service.novel.downloader.instance;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelInfo;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloaderInfo;
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
 * 三五 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 22:11
 */
@Component
@Slf4j
public class ThreeFiveNovelDownloader implements NovelDownloader {

    private final NovelDownloaderInfo info = new NovelDownloaderInfo()
            .setWebName("三五中文")
            .setMark("ThreeFive")
            .setWebsite("https://www.xkushu.com")
            .setContentBaseUrl("https://www.xkushu.com/")
            .setCatalogueBaseUrl("https://www.xkushu.com/")
            .setSearchBaseUrl("https://www.xkushu.com/modules/article/search.php")
            .setDisable(true);

    @Override
    public NovelDownloaderInfo getInfo() {
        return info;
    }

    @Override
    public QueryNetworkNovelInfo queryNovel(String searchText) {
        try {
            List<NetworkNovelInfo> novelInfoList = new ArrayList<>();
            Document doc = Jsoup.connect(info.getSearchBaseUrl())
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .data("searchkey", searchText)
                    .data("action", "login")
                    .post();
            Element content = doc.getElementById("content");
            if (content == null) {
                return new QueryNetworkNovelInfo()
                        .setErrorMsg("id 为 content 的 dom 不存在");
            }
            Element tBody = content.getElementsByTag("tbody").getFirst();
            Elements trList = tBody.getElementsByTag("tr");
            for (int i = 1; i < trList.size() && i < 6; i++) {
                NetworkNovelInfo novelInfo = findNovelInfoFromTr(trList.get(i));
                if (novelInfo != null) {
                    novelInfoList.add(novelInfo
                            .setStorageKey(UuidUtil.genId())
                            .setDownloaderMark(info.getMark()));
                }
            }
            return new QueryNetworkNovelInfo()
                    .setSourceName(info.getWebName())
                    .setSourceWebsite(info.getWebsite())
                    .setDownloaderMark(info.getMark())
                    .setWebSearch(info.getSearchBaseUrl())
                    .setNovelInfoList(novelInfoList);
        } catch (IOException e) {
            log.error("查询发生异常", e);
            return new QueryNetworkNovelInfo()
                    .setErrorMsg("查询发生异常");
        }
    }

    private NetworkNovelInfo findNovelInfoFromTr(Element tr) {
        NetworkNovelInfo novelInfo = new NetworkNovelInfo();
        Elements tdList = tr.getElementsByTag("td");
        if (tdList.isEmpty()) {
            return null;
        }

        Element td = tdList.get(0);
        Element a = td.getElementsByTag("a").getFirst();
        novelInfo.setName(a.text());
        novelInfo.setCatalogueUrl(a.attr("href"));
        if (tdList.size() > 2) {
            td = tdList.get(2);
            novelInfo.setAuthor(td.text());
        }
        return novelInfo;
    }

    @Override
    public List<NetworkNovelChapter> findChapterList(String catalogueUrl) {
        try {
            List<NetworkNovelChapter> chapterList = new ArrayList<>();
            Document doc = Jsoup.connect(info.getWebsite() + catalogueUrl)
                    .timeout(Constant.Network.CONNECT_TIMEOUT)
                    .headers(Constant.Network.CHROME_HEADERS)
                    .get();
            Element list = doc.getElementById("list");
            if (list == null) {
                return Collections.emptyList();
            }
            Elements ddList = list.getElementsByTag("dd");
            for (int i = 0; i < ddList.size(); i++) {
                Element dd = ddList.get(i);
                Elements aList = dd.getElementsByTag("a");
                if (aList.isEmpty()) {
                    continue;
                }
                Element a = dd.getElementsByTag("a").getFirst();
                String href = a.attr("href");
                chapterList.add(new NetworkNovelChapter()
                        .setIndex(i)
                        .setName(a.text())
                        .setUrl(catalogueUrl + "/" + href));
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
                    .setContent(handleContent(content.text()))
                    .setContentHtml(handleContent(content.html()));
        } catch (IOException e) {
            return new NetworkNovelContent()
                    .setErrorMsg("获取小说内容发生异常, " + e.getLocalizedMessage());
        }
    }

    @Override
    public String handleContent(String content) {
        String space = "\s\s\s\s";
        return content.replace(space, "\n");
    }
}
