package com.moriaty.vuitton.bean.novel;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:18
 */
@Data
@Accessors(chain = true)
public class NovelInfo {

    private String name;

    private String author;

    private String intro;

    private String imgUrl;

    private String chapterUrl;

}
