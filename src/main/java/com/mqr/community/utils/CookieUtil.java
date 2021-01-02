package com.mqr.community.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getCookie(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数异常");
        }
        Cookie[] cookies = request.getCookies();
        String ticket = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    ticket = cookie.getValue();
                }
            }
        }

        return ticket;
    }
}
