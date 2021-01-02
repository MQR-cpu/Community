package com.mqr.community.service;

import com.mqr.community.dao.LoginTicketMapper;
import com.mqr.community.dao.UserMapper;
import com.mqr.community.entity.LoginTicket;
import com.mqr.community.entity.User;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import com.mqr.community.utils.MailClient;
import com.mqr.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService implements CommunityConstant {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.domain.path}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Resource
//    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id) {
        User u = getCathe(id);
        if (u == null) {
            u = initCathe(id);
        }
        return u;
    }


    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("user is NULL");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("nameMsg", "用户名不合法");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不合法");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不合法");
            return map;
        }

        //用户名是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("nameMsg", "用户名已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已注册");
            return map;
        }

        //添加用户
        user.setActivationCode(CommunityUtil.getUUID());
        user.setCreateTime(new Date());
        user.setStatus(0);
        user.setType(0);
        //http://images.nowcoder.com/head/12t.png
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setSalt(CommunityUtil.getUUID().substring(0, 5));
        user.setPassword(CommunityUtil.getMD5(user.getPassword() + user.getSalt()));

        userMapper.insertUser(user);


        //采用多线程  减少用户等待时间
        new Thread(new Runnable() {
            @Override
            public void run() {
                //发送激活邮件
                Context context = new Context();
                context.setVariable("email", user.getEmail());
                // http://localhost:8080/activation/101/code
                String url = domain + contextPath + "activation/" + user.getId() + "/" + user.getActivationCode();
                context.setVariable("url", url);
                String content = engine.process("activation", context);
                mailClient.sendMail(user.getEmail(), "激活账号", content);
            }
        }).start();


        return map;
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("password", "密码不能为空");
            return map;
        }

        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "用户账号或密码错误");
            map.put("passwordMsg", "用户账号或密码错误");
            return map;
        }
        String p = CommunityUtil.getMD5(password + user.getSalt());
        if (!user.getPassword().equals(p)) {
            map.put("usernameMsg", "用户账号或密码错误");
            map.put("passwordMsg", "用户账号或密码错误");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活");
            return map;
        }

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setTicket(CommunityUtil.getUUID());

        //存到redis中
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return 0;
        } else if (user.getStatus() == 1) {
            return 1;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            deleteCathe(userId);
            return 2;
        } else {
            return 3;
        }
    }

    public void logout(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(0);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    public int updateHeader(int userId,String path) {


        int i = userMapper.updateHeader(userId, path);
        deleteCathe(userId);
        return i;
    }

    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    //缓存数据三部曲

    //1.先从缓存中取
    private User getCathe(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //2.初始化缓存
    private User initCathe(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user);
        return user;
    }

    //3.删除缓存
    private void deleteCathe(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
