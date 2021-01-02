package com.mqr.community.controller;

import com.mqr.community.utils.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class DemoController {


    @RequestMapping(value = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String cookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("mqr", CommunityUtil.getUUID());
        cookie.setMaxAge(60*5);
        cookie.setPath("/cookie/get");
        response.addCookie(cookie);
        return "set cookie";
    }

    @RequestMapping(value = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String cookieGet(@CookieValue("mqr") String cookie) {

        return cookie;
    }

    @RequestMapping("/session/set")
    @ResponseBody
    public String session(HttpSession session) {
        session.setAttribute("id","123456");
        return "session set";
    }
}
