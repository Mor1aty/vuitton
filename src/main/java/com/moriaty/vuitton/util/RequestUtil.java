package com.moriaty.vuitton.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

/**
 * <p>
 * 请求 Util
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 16:58
 */
@Slf4j
public class RequestUtil {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .connectTimeout(Duration.ofMinutes(5))
            .writeTimeout(Duration.ofMinutes(5))
            .readTimeout(Duration.ofMinutes(5))
            .callTimeout(Duration.ofMinutes(5))
            .build();

    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    private RequestUtil() {

    }

    public static String sendRequest(String url, String method, String reqBody) throws IOException {
        Request.Builder reqBuilder = new Request.Builder().url(url);
        if (METHOD_GET.equalsIgnoreCase(method)) {
            reqBuilder.method(method, null);
        } else {
            reqBuilder.method(method,
                    RequestBody.create(reqBody, MediaType.parse("application/json; charset=UTF-8")));
        }
        Request req = reqBuilder.build();
        try (Response response = HTTP_CLIENT.newCall(req).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }
}
