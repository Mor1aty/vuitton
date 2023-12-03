package com.moriaty.vuitton.service.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.dao.mapper.SettingMapper;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 小说 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 17:39
 */
@Service
@AllArgsConstructor
@Slf4j
public class NovelService {

    private final SettingMapper settingMapper;

    public Wrapper<List<Setting>> findSetting(String key) {
        LambdaQueryWrapper<Setting> queryWrapper = new LambdaQueryWrapper<Setting>().orderByDesc(Setting::getId);
        if (StringUtils.hasText(key)) {
            queryWrapper.like(Setting::getKey, key);
        }
        List<Setting> settingList = settingMapper.selectList(queryWrapper);
        return WrapMapper.ok("获取成功", settingList);
    }

    public Wrapper<String> insertSetting(String key, String value) {
        String id = UuidUtil.genId();
        settingMapper.insert(new Setting().setId(UuidUtil.genId()).setKey(key).setValue(value));
        return WrapMapper.ok("插入成功", id);
    }

    public Wrapper<Void> updateSetting(String id, String value) {
        settingMapper.updateById(new Setting().setId(id).setValue(value));
        return WrapMapper.ok("更新成功");
    }
}