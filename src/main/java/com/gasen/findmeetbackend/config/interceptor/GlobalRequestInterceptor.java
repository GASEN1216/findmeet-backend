package com.gasen.findmeetbackend.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.gasen.findmeetbackend.constant.UserConstant.USER_LOGIN_IN;

/**
 * 全局请求拦截器
 * @author GASEN
 * @date 2024/3/1 22:34
 * @classType description
 */
public class GlobalRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取HttpSession对象
        HttpSession session = request.getSession(false);

        // 检查session是否存在
        if (session == null) {
            // 如果session不存在，可以重定向到登录页面或返回错误信息
            response.sendRedirect("/login");
            return false;
        }

        // 检查session是否有效，例如检查session中是否有用户认证信息
        Object user = session.getAttribute(USER_LOGIN_IN);
        if (user == null) {
            // 如果session存在但没有有效的用户信息，说明session无效或者已过期，结束这个session
            session.invalidate();
            response.sendRedirect("/login");
            return false;
        }

        // 如果session存在且有效，则允许请求继续处理
        return true;
    }
}
