package com.fmk.student.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmk.student.common.Result;
import com.fmk.student.common.enums.ResultCodeEnum;
import com.fmk.student.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 获取 Token
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = jwtUtil.extractToken(authHeader);
        } else {
            token = request.getParameter("token");
        }

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. 解析 Token
            if (!jwtUtil.validateToken(token)) {
                writeErrorResponse(response, "Token 已过期或无效，请重新登录");
                return;
            }

            // 3. 提取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (!StringUtils.hasText(username)) {
                writeErrorResponse(response, "Token 解析异常");
                return;
            }

            // 4. 从 Redis 中获取完整的登录用户信息 (双重校验机制)
            String redisKey = "login_user:" + username;
            LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(redisKey);
            if (loginUser == null) {
                writeErrorResponse(response, "登录已失效，请重新登录");
                return;
            }

            // 5. 构造 SecurityContext 所需的认证主体，并将权限集 (Authorities) 注入
            // 注意：这里将整个 LoginUser 对象作为 principal，方便后续获取用户信息
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginUser, null, loginUser.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Token 校验异常: ", e);
            writeErrorResponse(response, "登录状态异常，请重新登录");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<?> result = Result.fail(ResultCodeEnum.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
