package com.moriaty.vuitton.core.wrap;

/**
 * <p>
 * 封装常量
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:00
 */
public class WrapConstant {

    private WrapConstant() {

    }

    /**
     * 成功 code
     */
    public static final int CODE_SUCCESS = 200;

    /**
     * 成功 msg
     */
    public static final String MSG_SUCCESS = "成功";

    /**
     * 失败 code
     */
    public static final int CODE_FAILURE = 201;

    /**
     * 失败 msg
     */
    public static final String MSG_FAILURE = "失败";

    /**
     * 参数异常 code
     */
    public static final int CODE_ILLEGAL_PARAM = 400;

    /**
     * 不支持的方法 code
     */
    public static final int CODE_ILLEGAL_METHOD = 405;

    /**
     * 不支持的方法 msg
     */
    public static final String MSG_ILLEGAL_METHOD = "不支持的方法";

    /**
     * 请求异常 code
     */
    public static final int CODE_ERROR = 500;

    /**
     * 请求异常 msg
     */
    public static final String MSG_ERROR = "请求异常";

}
