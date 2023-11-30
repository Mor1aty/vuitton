package com.moriaty.vuitton.core.log;

import java.lang.annotation.*;

/**
 * <p>
 * 视图日志注解
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/1 上午2:20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ViewLog {
}
