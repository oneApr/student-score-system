package com.fmk.student.common.exception;

import com.fmk.student.common.Result;
import com.fmk.student.common.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义业务异常拦截
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * DTO 参数校验异常拦截
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", errorMsg);
        return Result.fail(ResultCodeEnum.VALIDATE_FAILED.getCode(), errorMsg);
    }

    /**
     * 请求体解析异常拦截
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("请求体解析异常: {}", e.getMessage());
        return Result.fail(ResultCodeEnum.VALIDATE_FAILED.getCode(), "请求体格式错误或为空");
    }

    /**
     * 未知运行时异常拦截
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统内部异常: ", e);
        // 返回具体错误信息，方便调试
        String errorMessage = e.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = e.getClass().getSimpleName();
        }
        // 限制错误信息长度
        if (errorMessage.length() > 200) {
            errorMessage = errorMessage.substring(0, 200);
        }
        return Result.fail(ResultCodeEnum.ERROR.getCode(), "系统错误: " + errorMessage);
    }
}
