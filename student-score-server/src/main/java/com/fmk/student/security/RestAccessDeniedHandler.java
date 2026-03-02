package com.fmk.student.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmk.student.common.Result;
import com.fmk.student.common.enums.ResultCodeEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<?> result = Result.fail(ResultCodeEnum.FORBIDDEN);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
