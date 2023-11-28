package com.moriaty.vuitton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * <p>
 * 服务配置
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 上午4:24
 */
@Configuration
public class ServerConfig {

    @Bean
    public ThymeleafCustomDialect customThymeleafDialect() {
        return new ThymeleafCustomDialect("custom");
    }

}
