package com.fmk.student.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmk.student.common.Result;
import com.fmk.student.common.enums.ResultCodeEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<?> result = Result.fail(ResultCodeEnum.UNAUTHORIZED.getCode(), "未登录或登录已失效，请先登录");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
