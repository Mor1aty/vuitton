package com.moriaty.vuitton.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;


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
    public FilterRegistrationBean<CrossDomainFilter> crossDomainFilter() {
        FilterRegistrationBean<CrossDomainFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CrossDomainFilter());
        registrationBean.setUrlPatterns(Collections.singletonList("/*"));
        return registrationBean;
    }

    @Bean
    public ThymeleafCustomDialect customThymeleafDialect() {
        return new ThymeleafCustomDialect("custom");
    }

}
