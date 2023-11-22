package com.moriaty.vuitton;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务信息
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 23:08
 */
public class ServerInfo {

    private ServerInfo() {

    }

    public static final BaseInfo BASE_INFO = new BaseInfo();

    @Data
    @Accessors(chain = true)
    public static class BaseInfo {

        private String serverUrl;

        private String serverHost;

        private String serverPort;

        private String fileServerUrl;

    }
}
