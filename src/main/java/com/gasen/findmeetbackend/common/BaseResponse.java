package com.gasen.findmeetbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 错误码
     * */
    private int code;
    /**
     * 错误信息
     * */
    private String message;
    /**
     * 数据
     * */
    private T data;
    /**
     * 详细信息
     * */
    private String detail;


    public BaseResponse(int code, String message, T data, String detail) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.detail = detail;
    }

    public BaseResponse(ErrorCode errorCode, T data) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = data;
        this.detail = errorCode.getDetail();
    }

    public BaseResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = errorCode.getDetail();
    }

}
