package com.moriaty.vuitton.module.novel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.dao.mapper.SettingMapper;
import com.moriaty.vuitton.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 小说模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/18 17:55
 */
@Service("novelModule")
@RequiredArgsConstructor
@Slf4j
public class NovelModule {

    private final SettingMapper settingMapper;

    public List<Setting> findSetting(String id, String key) {
        LambdaQueryWrapper<Setting> queryWrapper = new LambdaQueryWrapper<Setting>().orderByDesc(Setting::getId);
        if (StringUtils.hasText(id)) {
            queryWrapper.eq(Setting::getId, id);
        }
        if (StringUtils.hasText(key)) {
            queryWrapper.eq(Setting::getKey, key);
        }
        return settingMapper.selectList(queryWrapper);
    }

    public String insertSetting(String key, String value) {
        String id = UuidUtil.genId();
        settingMapper.insert(new Setting().setId(UuidUtil.genId()).setKey(key).setValue(value));
        return id;
    }

    public void updateSetting(String id, String value) {
        settingMapper.updateById(new Setting().setId(id).setValue(value));
    }
}
