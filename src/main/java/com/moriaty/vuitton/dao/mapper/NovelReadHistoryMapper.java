package com.moriaty.vuitton.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moriaty.vuitton.bean.novel.local.NovelReadHistoryInfo;
import com.moriaty.vuitton.dao.entity.NovelReadHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 小说阅读历史 Mapper
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 14:39
 */
public interface NovelReadHistoryMapper extends BaseMapper<NovelReadHistory> {

    /**
     * 查询小说阅读记录详情
     *
     * @param novelId String
     * @return List with VideoViewHistoryInfo
     */
    @Select("""
            <script>
            SELECT
                nrh.id AS readId,
                nrh.read_time AS readTime,
                nrh.novel AS novelId,
                nc.`index` AS chapterIndex,
                nc.name AS chapterName
            FROM novel_read_history nrh
            LEFT JOIN novel_chapter nc ON nrh.novel = nc.novel AND nrh.chapter_index = nc.`index`
            WHERE 1 = 1
            <if test="novelId != null and novelId != ''"> AND nrh.novel = #{novelId} </if>
            ORDER BY nrh.read_time DESC
            </script>""")
    List<NovelReadHistoryInfo> findNovelReadHistory(@Param("novelId") String novelId);
}
