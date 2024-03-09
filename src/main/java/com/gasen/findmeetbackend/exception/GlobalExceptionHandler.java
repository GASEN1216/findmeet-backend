package com.gasen.findmeetbackend.exception;

import com.gasen.findmeetbackend.common.BaseResponse;
import com.gasen.findmeetbackend.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessExcetion.class)
    public BaseResponse businessExceptionHandler(BusinessExcetion e) {
        log.error("业务异常："+e.getDescription(), e);
        return new BaseResponse(e.getCode(), e.getMessage(), null, e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常：", e);
        return new BaseResponse(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }

}
