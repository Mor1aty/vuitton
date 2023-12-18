package com.moriaty.vuitton.view;

import com.moriaty.vuitton.bean.novel.NovelSetting;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.module.novel.NovelModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * 小说视图
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 17:33
 */
@Controller
@RequestMapping("novel")
@AllArgsConstructor
@Slf4j
public class NovelView {

    private final NovelModule novelModule;

    @RequestMapping
    public String novel() {
        return "redirect:novel/local";
    }

    @RequestMapping("setting")
    @ViewLog
    public String novelSetting(Model model, @RequestParam("back") String back,
                               @RequestParam(value = "settingId", required = false) String settingId,
                               @RequestParam(value = "settingValue", required = false) String settingValue) {

        if (StringUtils.hasText(settingId) && StringUtils.hasText(settingValue)) {
            novelModule.updateSetting(settingId, settingValue);
            log.info("{} 更新成功", settingId);
            model.addAttribute("saveSettingStart", true);
        }
        NovelSetting setting = new NovelSetting();
        List<Setting> settingList = novelModule.findSetting(null, Constant.Setting.NOVEL_CONTENT_FONT_SIZE);
        if (!settingList.isEmpty()) {
            setting.setContentFontSize(settingList.getFirst());
        }
        model.addAttribute("setting", setting);
        model.addAttribute("back", back);
        return "novel/novel_setting";
    }
}
