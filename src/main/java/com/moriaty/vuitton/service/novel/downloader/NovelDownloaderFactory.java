package com.moriaty.vuitton.service.novel.downloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 小说工厂
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:39
 */
public class NovelDownloaderFactory {

    private NovelDownloaderFactory() {

    }

    private static final Map<String, NovelDownloader> FACTORY_DOWNLOADER_MAP = new HashMap<>();

    private static final Map<String, NovelDownloaderInfo> FACTORY_DOWNLOADER_INFO_MAP = new HashMap<>();

    public static void putDownloaderMap(String mark, NovelDownloader novelDownloader) {
        FACTORY_DOWNLOADER_MAP.put(mark, novelDownloader);
        FACTORY_DOWNLOADER_INFO_MAP.put(mark, novelDownloader.getInfo());
    }

    public static void putDownloaderMap(Map<String, NovelDownloader> novelDownloaderMap) {
        FACTORY_DOWNLOADER_MAP.putAll(novelDownloaderMap);
        novelDownloaderMap.forEach((mark, downloader) -> FACTORY_DOWNLOADER_INFO_MAP.put(mark, downloader.getInfo()));
    }

    public static NovelDownloader getDownloader(String mark) {
        return FACTORY_DOWNLOADER_MAP.get(mark);
    }

    public static NovelDownloaderInfo getDownloaderInfo(String mark) {
        return FACTORY_DOWNLOADER_INFO_MAP.get(mark);
    }

    public static List<NovelDownloader> getAllDownloader() {
        return new ArrayList<>(FACTORY_DOWNLOADER_MAP.values());
    }

    public static List<NovelDownloaderInfo> getAllDownloaderInfo() {
        return new ArrayList<>(FACTORY_DOWNLOADER_INFO_MAP.values());
    }
}
