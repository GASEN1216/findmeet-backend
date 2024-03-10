package com.gasen.findmeetbackend.config;

import com.gasen.findmeetbackend.config.interceptor.GlobalRequestInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器类
 * @author GASEN
 * @date 2024/3/1 22:50
 * @classType description
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //全局请求拦截器
        registry.addInterceptor(new GlobalRequestInterceptor())
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/current"); // 排除不需要检查session的路径
    }

}
