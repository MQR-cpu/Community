package com.mqr.community.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.mqr.community.entity.HostHolder;
import com.mqr.community.entity.User;
import com.mqr.community.service.UserService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import com.mqr.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserService userService;

    @Autowired
    private DefaultKaptcha kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        return "register";
    }

    @RequestMapping(path = "/register" ,method = RequestMethod.POST)
    public String register(Model model, User user) {

        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "已给您发送一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "operate-result";
        } else {
            model.addAttribute("nameMsg", map.get("nameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "register";
        }

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {

        return "login";
    }


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(Model model, String password, String username,
                        String code,boolean rememberMe,
                        HttpServletResponse response,
                        @CookieValue("kaptcha") String kaptcha) {

        String SessionCode = null;
        if (StringUtils.isNotBlank(kaptcha)) {
            String key = RedisKeyUtil.getKaptha(kaptcha);
            SessionCode = (String)redisTemplate.opsForValue().get(key);
        }

//        String SessionCode = (String)session.getAttribute("kaptcha");


        if (StringUtils.isBlank(SessionCode) || StringUtils.isBlank(code)|| !code.equalsIgnoreCase(SessionCode)) {
            model.addAttribute("codeMsg", "验证码错误");
            return "login";
        }
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", (String)map.get("ticket"));
            cookie.setPath("/");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "login";
        }

    }


    // http://localhost:8080/activation/101/code
    @RequestMapping(path = ("/activation/{userId}/{code}"))
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_ERROR) {
            model.addAttribute("msg", "无效操作,该账号无法激活!");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_FAILURE) {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "operate-result";
    }

    @RequestMapping(value = "/kaptcha",method = RequestMethod.GET)
    public void kaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
//        session.setAttribute("kaptcha", text);

        //给用户发个临时Cookie
        String owner = CommunityUtil.getUUID();
        Cookie cookie = new Cookie("kaptcha", owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //存到redis 过期失效
        String s = RedisKeyUtil.getKaptha(owner);
        //必须设置 时间的单位
        redisTemplate.opsForValue().set(s,text,60, TimeUnit.SECONDS);

        // 将突图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }
}
