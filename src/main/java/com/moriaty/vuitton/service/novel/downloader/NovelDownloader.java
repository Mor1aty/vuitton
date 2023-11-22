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

    NovelDownloaderInfo getInfo();

    QueryNovelInfo queryNovel(String queryName);

    List<NovelChapter> findChapterList(String catalogueAppend);

    NovelContent findContent(String chapterName, String contentAppend);

    String handleContent(String content);

}
