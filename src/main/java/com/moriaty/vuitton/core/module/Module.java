package com.moriaty.vuitton.core.module;

import java.lang.annotation.*;

/**
 * <p>
 * 模块注解
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:52
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Module {

    /**
     * id 从 0 开始
     *
     * @return int
     */
    int id();

    String name();

    String path();
}
