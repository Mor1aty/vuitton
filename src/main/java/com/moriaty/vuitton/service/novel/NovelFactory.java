package com.moriaty.vuitton.service.novel;

import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;

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
public class NovelFactory {

    private NovelFactory() {

    }

    private static final Map<String, NovelDownloader> FACTORY_DOWNLOADER_MAP = new HashMap<>();

    public static void putDownloaderMap(String mark, NovelDownloader novelDownloader) {
        FACTORY_DOWNLOADER_MAP.put(mark, novelDownloader);
    }

    public static void putDownloaderMap(Map<String, NovelDownloader> novelDownloaderMap) {
        FACTORY_DOWNLOADER_MAP.putAll(novelDownloaderMap);
    }

    public static NovelDownloader getDownloader(String mark) {
        return FACTORY_DOWNLOADER_MAP.get(mark);
    }

    public static List<NovelDownloader> getAllDownloader() {
        return new ArrayList<>(FACTORY_DOWNLOADER_MAP.values());
    }
}
