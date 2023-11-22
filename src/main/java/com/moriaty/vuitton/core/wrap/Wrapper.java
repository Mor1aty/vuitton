package com.moriaty.vuitton.core.wrap;

/**
 * <p>
 * 返回包装
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 15:57
 */
public record Wrapper<T>(int code, String msg, T data) {
}
