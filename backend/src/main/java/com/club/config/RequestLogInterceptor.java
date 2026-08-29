package com.club.config;

import com.club.common.SessionKeys;
import com.club.dto.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger("ACCESS_LOG");
    private static final String START_TIME = RequestLogInterceptor.class.getName() + ".startTime";
    private static final String LOGIN_USER = RequestLogInterceptor.class.getName() + ".loginUser";
    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID, traceId);
        response.setHeader("X-Request-Id", traceId);
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute(LOGIN_USER, session.getAttribute(SessionKeys.LOGIN_USER));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        Object startValue = request.getAttribute(START_TIME);
        long duration = startValue instanceof Long start ? System.currentTimeMillis() - start : -1L;
        LoginUser user = request.getAttribute(LOGIN_USER) instanceof LoginUser loginUser ? loginUser : null;
        if (user == null) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(SessionKeys.LOGIN_USER) instanceof LoginUser loginUser) {
                user = loginUser;
            }
        }
        log.info("method={} path={} status={} userId={} username={} ip={} durationMs={} exception={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(),
                user == null ? "anonymous" : user.getId(),
                user == null ? "anonymous" : user.getUsername(),
                clientIp(request), duration, exception == null ? "none" : exception.getClass().getSimpleName());
        MDC.remove(TRACE_ID);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
