package com.moriaty.vuitton.config;

import com.moriaty.vuitton.core.wrap.WrapMapper;
import com.moriaty.vuitton.core.wrap.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * 全局异常处理
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/10 22:53
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {
    /**
     * 请求方法不支持异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.OK)
    public Wrapper<Object> requestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("illegal method: {}", e.getMethod());
        return WrapMapper.illegalMethod();
    }

    /**
     * 参数非法异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.OK)
    public Wrapper<Object> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.error("illegal argument: {}", msg);
        return WrapMapper.illegalParam(msg);
    }

    /**
     * 请求体异常异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.OK)
    public Wrapper<Object> requestBodyException(Exception e) {
        log.error("request body error", e);
        return WrapMapper.error();
    }

    /**
     * 异常.
     *
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Wrapper<Object> exception(Exception e) {
        log.error("server error", e);
        return WrapMapper.error();
    }
}
