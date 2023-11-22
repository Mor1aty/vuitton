package com.moriaty.vuitton.util;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Ffmpeg 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 17:32
 */
@Slf4j
public class FfmpegUtil {

    private FfmpegUtil() {

    }

    public static void findInputInfo(String input) {
        CommandRunnerUtil.Result result = CommandRunnerUtil.runCommand("ffmpeg -i " + input);
        log.info("获取输入文件信息结果: {}", result);
        if (result.getCode() != CommandRunnerUtil.Result.CODE_SUCCESS) {
            return;
        }
        log.info("{}", result.getOutput());
    }

}
