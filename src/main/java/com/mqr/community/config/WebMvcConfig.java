package com.mqr.community.config;

import com.mqr.community.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;
//
//    @Autowired
//    private RedirectLoginInterceptor redirectLoginInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/asserts/**","/webjars/**");;
//        registry.addInterceptor(redirectLoginInterceptor).addPathPatterns("/like").addPathPatterns("/follow").addPathPatterns("/unFollow");
//        registry.addInterceptor(loginRequiredInterceptor);
        registry.addInterceptor(messageInterceptor).addPathPatterns("/**").excludePathPatterns("/asserts/**","/webjars/**");;
        registry.addInterceptor(dataInterceptor).addPathPatterns("/**").excludePathPatterns("/asserts/**","/webjars/**");;
    }
}
