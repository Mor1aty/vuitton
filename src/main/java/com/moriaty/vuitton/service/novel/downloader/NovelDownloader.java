package com.moriaty.vuitton.service.novel.downloader;

import com.moriaty.vuitton.bean.novel.NovelChapter;
import com.moriaty.vuitton.bean.novel.NovelContent;
import com.moriaty.vuitton.bean.novel.QueryNovelInfo;

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
     * @param queryName String
     * @return QueryNovelInfo
     */
    QueryNovelInfo queryNovel(String queryName);

    /**
     * 查找目录
     *
     * @param catalogueAppend String
     * @return List with NovelChapter
     */
    List<NovelChapter> findChapterList(String catalogueAppend);

    /**
     * 查找正文
     *
     * @param chapterName   String
     * @param contentAppend String
     * @return NovelContent
     */
    NovelContent findContent(String chapterName, String contentAppend);

    /**
     * 处理正文
     *
     * @param content String
     * @return String
     */
    String handleContent(String content);

}
