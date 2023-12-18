package com.moriaty.vuitton.bean.common;

import com.moriaty.vuitton.core.module.Module;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 通用信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/8 1:26
 */
@Data
@Accessors(chain = true)
public class CommonInfo {

    private String lastAccess;

    private String fileServerUrl;

    private List<Module> moduleList;
}
