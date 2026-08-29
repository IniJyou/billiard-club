package com.club.config;

import com.club.common.AdminOnly;
import com.club.common.Result;
import com.club.common.SessionKeys;
import com.club.dto.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(SessionKeys.LOGIN_USER);
        if (user == null) {
            log.warn("event=access_denied reason=unauthenticated method={} path={} ip={}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            writeError(response, 401, "登录已失效，请重新登录");
            return false;
        }

        if (handler instanceof HandlerMethod method
                && (method.hasMethodAnnotation(AdminOnly.class)
                || method.getBeanType().isAnnotationPresent(AdminOnly.class))
                && user.getRole() != 1) {
            log.warn("event=access_denied reason=forbidden userId={} username={} method={} path={}",
                    user.getId(), user.getUsername(), request.getMethod(), request.getRequestURI());
            writeError(response, 403, "当前账号没有管理员权限");
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
