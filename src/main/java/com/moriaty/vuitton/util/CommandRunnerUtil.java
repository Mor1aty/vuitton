package com.moriaty.vuitton.util;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>
 * 命令执行 Util
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 17:59
 */
@Slf4j
public class CommandRunnerUtil {

    private CommandRunnerUtil() {

    }

    public static Result runCommand(String command) {
        Result result = new Result().setCommand(command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
            int exitCode = process.waitFor();
            process.destroy();
            return result.setCode(exitCode).setOutput(sb.toString());
        } catch (IOException e) {
            log.error("执行命令 {} 异常", command, e);
            return result.setCode(Result.CODE_ERROR).setOutput(null);
        } catch (InterruptedException e) {
            log.error("执行命令 {} 被打断", command, e);
            Thread.currentThread().interrupt();
            return result.setCode(Result.CODE_ERROR).setOutput(null);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Result {

        public static final int CODE_ERROR = -1;

        public static final int CODE_SUCCESS = 0;

        private String command;

        private int code;

        private String output;
    }
}
