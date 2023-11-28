package com.moriaty.vuitton.util;

import org.springframework.ui.Model;

/**
 * <p>
 * View 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 下午3:42
 */
public class ViewUtil {

    private ViewUtil() {

    }

    public static String goError(Model model, String errorMsg, String data) {
        model.addAttribute("errorMsg", errorMsg);
        model.addAttribute("data", data);
        return "error";
    }
}
