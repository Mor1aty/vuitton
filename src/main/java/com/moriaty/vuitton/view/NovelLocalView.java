package com.moriaty.vuitton.view;

import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import com.moriaty.vuitton.dao.entity.Novel;
import com.moriaty.vuitton.service.novel.NovelLocalService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 本地小说 Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 2:46
 */
@Controller
@RequestMapping("novel/local")
@AllArgsConstructor
@Slf4j
public class NovelLocalView implements InitializingBean {

    private final NovelLocalService novelLocalService;

    @Override
    public void afterPropertiesSet() {
        ModuleFactory.addModule(new Module()
                .setId(1)
                .setName("本地小说")
                .setPath("novel/local"));
    }

    @RequestMapping
    @ViewLog
    public String novel(Model model,
                        @RequestParam(value = "searchText", required = false) String searchText) {
        if (StringUtils.hasText(searchText)) {
            Wrapper<List<Novel>> novelWrapper = novelLocalService.findNovel(searchText);
            model.addAttribute("novelList", WrapMapper.isOk(novelWrapper) ?
                    novelWrapper.data() : Collections.emptyList());
        }
        model.addAttribute("searchText", searchText);
        return "novel/local/local_novel";
    }

}
