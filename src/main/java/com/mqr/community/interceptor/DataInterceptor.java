package com.mqr.community.interceptor;

import com.mqr.community.entity.HostHolder;
import com.mqr.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //uv
        String ip = request.getRemoteHost();
        dataService.addUv(ip);

        //dav
        if (hostHolder.getUser() != null) {
             dataService.addDau(hostHolder.getUser().getId());
        }

        return true;
    }
}
