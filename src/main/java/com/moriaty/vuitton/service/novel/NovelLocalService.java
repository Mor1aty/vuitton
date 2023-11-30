package com.moriaty.vuitton.service.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
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

    public Wrapper<List<Novel>> findNovel(String name) {
        LambdaQueryWrapper<Novel> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Novel::getName, name);
        }
        List<Novel> novelList = novelMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", novelList);
    }

}
