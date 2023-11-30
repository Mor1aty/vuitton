package com.moriaty.vuitton.core.log;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 视图日志切面
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/1 上午1:56
 */
@Aspect
@Component
@Slf4j
public class ViewLogAspect {

    @Around("@annotation(com.moriaty.vuitton.core.log.ViewLog)")
    public Object actionAround(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Object[] argList = point.getArgs();
        String[] argNameList = ((CodeSignature) point.getSignature()).getParameterNames();
        Map<String, Object> param = HashMap.newHashMap(argList.length);
        for (int i = 0; i < argList.length; i++) {
            if (argList[i] instanceof Model) {
                continue;
            }
            param.put(argNameList[i], argList[i]);
        }
        log.info("View: {}, Req: {}", request.getRequestURI(), JSON.toJSONString(param));
        Object resp = point.proceed();
        log.info("View Resp: {}.html", resp);
        return resp;
    }
}
