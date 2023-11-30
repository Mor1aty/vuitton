package com.moriaty.vuitton.view;

import com.moriaty.vuitton.core.log.ViewLog;
import com.moriaty.vuitton.core.module.ModuleFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * <p>
 * Index Ctrl
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午3:41
 */
@Controller
@AllArgsConstructor
@Slf4j
public class IndexView {

    @RequestMapping({"/", "index"})
    @ViewLog
    public String index(final Model model) {
        model.addAttribute("moduleList", ModuleFactory.getAllModule());
        return "index";
    }
}
