package com.wanna.webmanagement.interceptor;

import com.wanna.webmanagement.util.BaseContext;
import com.wanna.webmanagement.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            log.warn("未携带token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Claims claims = jwtUtil.parseToken(token);
        if(claims==null){
            log.warn("token无效");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        BaseContext.setCurrentId(((Number)claims.get("id")).intValue());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
