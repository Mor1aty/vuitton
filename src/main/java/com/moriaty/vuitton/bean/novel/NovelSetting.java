package com.moriaty.vuitton.bean.novel;

import com.moriaty.vuitton.dao.entity.Setting;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说设置
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 17:30
 */
@Data
@Accessors(chain = true)
public class NovelSetting {

    private Setting contentFontSize;
}
