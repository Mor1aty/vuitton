package com.moriaty.vuitton.service;

import com.moriaty.vuitton.ServerInfo;
import com.moriaty.vuitton.bean.common.CommonInfo;
import com.moriaty.vuitton.constant.Constant;
import com.moriaty.vuitton.core.module.ModuleFactory;
import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 通用 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/8 1:21
 */
@Service
@AllArgsConstructor
@Slf4j
public class CommonService {

    public Wrapper<CommonInfo> info() {
        return WrapMapper.ok(new CommonInfo()
                .setLastAccess(LocalDateTime.now().format(Constant.Date.FORMAT_RECORD_TIME))
                .setFileServerUrl(ServerInfo.BASE_INFO.getFileServerUrl())
                .setModuleList(ModuleFactory.getAllModule()));
    }
}
