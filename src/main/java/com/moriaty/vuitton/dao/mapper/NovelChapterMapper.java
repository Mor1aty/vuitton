package com.moriaty.vuitton.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moriaty.vuitton.dao.entity.NovelChapter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 小说章节 Mapper
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/30 下午2:06
 */
public interface NovelChapterMapper extends BaseMapper<NovelChapter> {

    /**
     * 批量插入
     *
     * @param chapterList List with NovelChapter
     */
    @Insert("""
            <script>
            INSERT INTO novel_chapter(id, novel, `index`, `name`, content, content_html)
            VALUES
            <foreach collection='chapterList' item='chapter' separator=','>
                (#{chapter.id}, #{chapter.novel}, #{chapter.index}, #{chapter.name},
                #{chapter.content}, #{chapter.contentHtml})
            </foreach>
            </script>""")
    void insertBatch(@Param("chapterList") List<NovelChapter> chapterList);
}
