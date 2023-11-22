package com.moriaty.vuitton;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * 启动
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 15:55
 */
@SpringBootApplication
@MapperScan("com.moriaty.vuitton.dao.mapper")
public class VuittonApplication {

    public static void main(String[] args) {
        SpringApplication.run(VuittonApplication.class, args);
    }

}
