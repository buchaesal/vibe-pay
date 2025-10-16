package com.vibepay.config;

import com.vibepay.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String SESSION_MEMBER_ID = "memberId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 요청은 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);

        // 세션이 없거나 memberId가 없으면 인증 실패
        if (session == null || session.getAttribute(SESSION_MEMBER_ID) == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }

        return true;
    }
}
