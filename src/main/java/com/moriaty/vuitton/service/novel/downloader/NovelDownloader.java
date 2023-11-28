package com.moriaty.vuitton.service.novel.downloader;

import com.moriaty.vuitton.bean.novel.network.NetworkNovelChapter;
import com.moriaty.vuitton.bean.novel.network.NetworkNovelContent;
import com.moriaty.vuitton.bean.novel.network.QueryNetworkNovelInfo;

import java.util.List;

/**
 * <p>
 * 小说 Downloader
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:31
 */
public interface NovelDownloader {

    /**
     * 获取信息
     *
     * @return NovelDownloaderInfo
     */
    NovelDownloaderInfo getInfo();

    /**
     * 查询小说
     *
     * @param searchText String
     * @return QueryNovelInfo
     */
    QueryNetworkNovelInfo queryNovel(String searchText);

    /**
     * 查找目录
     *
     * @param catalogueAppend String
     * @return List with NovelChapter
     */
    List<NetworkNovelChapter> findChapterList(String catalogueAppend);

    /**
     * 查找正文
     *
     * @param chapterName   String
     * @param contentAppend String
     * @return NovelContent
     */
    NetworkNovelContent findContent(String chapterName, String contentAppend);

    /**
     * 处理正文
     *
     * @param content String
     * @return String
     */
    String handleContent(String content);

}
