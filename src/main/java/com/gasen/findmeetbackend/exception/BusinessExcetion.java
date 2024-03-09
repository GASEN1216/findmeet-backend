package com.gasen.findmeetbackend.exception;

import com.gasen.findmeetbackend.common.ErrorCode;

/**
 * 自定义异常类
 * */
public class BusinessExcetion extends RuntimeException{
        private final int code;

        private final String description;

        public BusinessExcetion(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public BusinessExcetion(ErrorCode errorCode) {
            super(errorCode.getMessage());
            this.code = errorCode.getCode();
            this.description = errorCode.getDetail();
        }

        public BusinessExcetion(ErrorCode errorCode, String description) {
            super(errorCode.getMessage());
            this.code = errorCode.getCode();
            this.description = description;
        }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
