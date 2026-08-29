package com.club.common;

/**
 * 业务异常：业务逻辑校验不通过时抛出，由 GlobalExceptionHandler 统一转成 Result
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
