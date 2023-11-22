package com.moriaty.vuitton.bean.view;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:47
 */
@Data
@Accessors(chain = true)
public class Module {

    private int id;

    private String name;

    private String path;
}
