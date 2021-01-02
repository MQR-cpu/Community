package com.mqr.community.interceptor;

import com.mqr.community.entity.HostHolder;
import com.mqr.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor  implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (hostHolder != null &&hostHolder.getUser() != null && modelAndView != null) {
            modelAndView.addObject("unread",
                    (messageService.findNoticeUnreadCount(hostHolder.getUser().getId(), null) + messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null))==0?"":messageService.findNoticeUnreadCount(hostHolder.getUser().getId(), null) + messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null));
        }
    }
}
