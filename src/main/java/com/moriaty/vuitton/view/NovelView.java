package com.moriaty.vuitton.view;

import com.moriaty.vuitton.bean.novel.NovelSetting;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Setting;
import com.moriaty.vuitton.service.novel.NovelService;
import com.moriaty.vuitton.util.ViewUtil;
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

    private final NovelService novelService;

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
            Wrapper<Void> updateWrapper = novelService.updateSetting(settingId, settingValue);
            log.info("{} 更新{}", settingId, WrapMapper.isOk(updateWrapper) ? "成功" : "失败");
            model.addAttribute("saveSettingStart", true);
        }
        NovelSetting setting = new NovelSetting();
        Wrapper<List<Setting>> settingWrapper = novelService.findSetting(null,
                Constant.Setting.NOVEL_CONTENT_FONT_SIZE);
        if (WrapMapper.isFailure(settingWrapper)) {
            return ViewUtil.goError(model, "小说内容字体大小出问题啦");
        }
        if (!settingWrapper.data().isEmpty()) {
            setting.setContentFontSize(settingWrapper.data().getFirst());
        }
        model.addAttribute("setting", setting);
        model.addAttribute("back", back);
        return "novel/novel_setting";
    }
}
