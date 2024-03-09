package com.gasen.findmeetbackend.config.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;


/**
 * 日志拦截器
 * @author GASEN
 * @date 2024/3/1 23:18
 * @classType description
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();

            StringBuilder logMessage = new StringBuilder()
                    .append("\nRequest URL: ").append(request.getRequestURI())
                    .append("\nMethod: ").append(request.getMethod())
                    .append("\nClass: ").append(className)
                    .append("\nMethod: ").append(methodName);

            // 请求参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                logMessage.append("\nParam: ").append(entry.getKey()).append("=").append(StringUtils.join(entry.getValue(), ", "));
            }

            // 客户端IP地址
            String clientIp = request.getHeader("X-Forwarded-For");
            if (StringUtils.isBlank(clientIp)) {
                clientIp = request.getRemoteAddr();
            }
            logMessage.append("\nClient IP: ").append(clientIp).append("\n");

            LOGGER.info(logMessage.toString());
        }

        return true;
    }

    // 可以选择实现postHandle和afterCompletion方法，但这部分对于日志记录不是必需的
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}