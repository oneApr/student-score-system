package com.fmk.student.common.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统未知错误"),
    UNAUTHORIZED(401, "尚未登录或Token已过期"),
    FORBIDDEN(403, "没有操作权限"),
    VALIDATE_FAILED(400, "参数校验失败"),
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_PASSWORD_ERROR(20002, "用户名或密码错误"),
    USER_ACCOUNT_LOCKED(20003, "账号已被锁定"),
    COURSE_FULL(30001, "课程名额已满"),
    DATA_HAS_MODIFIED(40001, "数据已被其他用户修改，请刷新重试");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
