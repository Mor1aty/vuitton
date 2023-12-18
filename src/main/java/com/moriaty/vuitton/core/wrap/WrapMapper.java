package com.moriaty.vuitton.core.wrap;

/**
 * <p>
 * 返回封装映射
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 15:59
 */
public class WrapMapper {

    private WrapMapper() {

    }

    public static <E> Wrapper<E> ok() {
        return new Wrapper<>(WrapConstant.CODE_SUCCESS, WrapConstant.MSG_SUCCESS, null);
    }

    public static <E> Wrapper<E> ok(String msg) {
        return new Wrapper<>(WrapConstant.CODE_SUCCESS, msg, null);
    }

    public static <E> Wrapper<E> ok(E data) {
        return new Wrapper<>(WrapConstant.CODE_SUCCESS, WrapConstant.MSG_SUCCESS, data);
    }

    public static Wrapper<String> okStringData(String data) {
        return new Wrapper<>(WrapConstant.CODE_SUCCESS, WrapConstant.MSG_SUCCESS, data);
    }

    public static <E> Wrapper<E> ok(String msg, E data) {
        return new Wrapper<>(WrapConstant.CODE_SUCCESS, msg, data);
    }

    public static <E> Wrapper<E> illegalParam(String msg) {
        return new Wrapper<>(WrapConstant.CODE_ILLEGAL_PARAM, msg, null);
    }

    public static <E> Wrapper<E> illegalMethod() {
        return new Wrapper<>(WrapConstant.CODE_ILLEGAL_METHOD, WrapConstant.MSG_ILLEGAL_METHOD, null);
    }

    public static <E> Wrapper<E> error() {
        return new Wrapper<>(WrapConstant.CODE_ERROR, WrapConstant.MSG_ERROR, null);
    }

    public static <E> Wrapper<E> failure() {
        return new Wrapper<>(WrapConstant.CODE_FAILURE, WrapConstant.MSG_FAILURE, null);
    }

    public static <E> Wrapper<E> failure(String msg) {
        return new Wrapper<>(WrapConstant.CODE_FAILURE, msg, null);
    }

    public static <E> boolean isOk(Wrapper<E> wrapper) {
        return wrapper.code() == WrapConstant.CODE_SUCCESS;
    }

    public static <E> boolean isFailure(Wrapper<E> wrapper) {
        return !isOk(wrapper);
    }
}
