package com.gasen.findmeetbackend.common;

/**
 * 错误码
 * */
public enum ErrorCode {

    SUCCESS(0, "成功", ""),
    PARAMETER_ERROR(1000, "参数错误", ""),
    USER_NOT_EXIST(1001, "用户不存在", ""),
    USER_EXIST(1002, "用户已存在", ""),
    USER_NOT_LOGIN(1003, "用户未登录", ""),
    USER_NOT_LOGIN_OR_NOT_ADMIN(1004, "用户未登录或非管理员", ""),
    SYSTEM_ERROR(1005, "系统错误", ""),
    BANNED_USER(1006, "用户已被封禁", "");


    private final int code;
    /**
     * 状态码信息
     * */
    private final String message;
    /**
     * 状态码详情
     * */
    private final String detail;

    ErrorCode(int code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }
}
