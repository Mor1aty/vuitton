package com.moriaty.vuitton.service.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.bean.novel.local.LocalNovelAroundChapter;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import com.moriaty.vuitton.dao.mapper.NovelChapterMapper;
import com.moriaty.vuitton.dao.mapper.NovelMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 本地小说 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 下午2:34
 */
@Service
@AllArgsConstructor
@Slf4j
public class NovelLocalService {

    private final NovelMapper novelMapper;

    private final NovelChapterMapper novelChapterMapper;

    public Wrapper<List<Novel>> findNovel(String id, String name) {
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(id)) {
            queryWrapper.eq(Novel::getId, id);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Novel::getName, name);
        }
        List<Novel> novelList = novelMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", novelList);
    }

    public Wrapper<List<NovelChapter>> findCatalogue(String novelId) {
        List<NovelChapter> chapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId));
        return WrapMapper.ok("获取成功", chapterList);
    }

    public Wrapper<LocalNovelAroundChapter> findAroundChapter(String novelId, int chapterIndex) {
        LocalNovelAroundChapter aroundChapter = new LocalNovelAroundChapter();
        List<NovelChapter> chapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .eq(NovelChapter::getIndex, chapterIndex));
        if (chapterList == null || chapterList.isEmpty()) {
            return WrapMapper.failure("小说章节不存在");
        }
        aroundChapter.setChapter(chapterList.getFirst());
        List<NovelChapter> lessChapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .lt(NovelChapter::getIndex, chapterIndex)
                .orderByDesc(NovelChapter::getIndex));
        if (lessChapterList != null && !lessChapterList.isEmpty()) {
            aroundChapter.setPreChapter(lessChapterList.getFirst());
        }
        List<NovelChapter> greatChapterList = novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovel, novelId)
                .gt(NovelChapter::getIndex, chapterIndex)
                .orderByAsc(NovelChapter::getIndex));
        if (greatChapterList != null && !greatChapterList.isEmpty()) {
            aroundChapter.setNextChapter(greatChapterList.getFirst());
        }
        return WrapMapper.ok("获取成功", aroundChapter);
    }
}
