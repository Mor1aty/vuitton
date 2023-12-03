package com.moriaty.vuitton.bean.novel;

import com.moriaty.vuitton.dao.entity.Setting;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说内容设置
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 17:30
 */
@Data
@Accessors(chain = true)
public class NovelContentSetting {

    private Setting contentFontSize;
}
