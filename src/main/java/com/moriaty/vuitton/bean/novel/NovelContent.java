package com.moriaty.vuitton.bean.novel;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说内容
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 21:52
 */
@Data
@Accessors(chain = true)
public class NovelContent {

    private String errorMsg;

    private String title;

    private String content;

    private String contentHtml;

}
