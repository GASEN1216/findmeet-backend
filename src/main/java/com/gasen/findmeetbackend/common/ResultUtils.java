package com.gasen.findmeetbackend.common;

/**
 * 返回工具类
 * */
public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode, String detail) {
        return new BaseResponse<>(errorCode, detail);
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode, T data) {
        return new BaseResponse<>(errorCode, data);
    }
    public static BaseResponse error(int code, String message, String detail) {
        return new BaseResponse<>(code, message, null,detail);
    }
}
