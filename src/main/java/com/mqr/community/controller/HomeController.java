package com.mqr.community.controller;

import com.mqr.community.entity.DiscussPost;
import com.mqr.community.entity.HostHolder;
import com.mqr.community.entity.PageBean;
import com.mqr.community.entity.User;
import com.mqr.community.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String init() {
        return "forward:/index";
    }

    @RequestMapping(path = {"/index"},method = RequestMethod.GET)
    public String index(Model model , PageBean page,
                        @RequestParam(value = "orderMode",defaultValue = "0") int orderMode) {

        User user = hostHolder.getUser();

        page.setRows(discussPostService.findDiscussPostCounts(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<Map<String, Object>> list = new ArrayList<>();
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        for (DiscussPost discussPost : discussPosts) {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("post", discussPost);
            stringObjectHashMap.put("postLikeCount", likeService.entityLikeCount(11, discussPost.getId()));
            if (user == null) {
                stringObjectHashMap.put("postLikeStatus", 0);
            } else {
                stringObjectHashMap.put("postLikeStatus", likeService.userLikeStatus(user.getId(),11, discussPost.getId()));
            }
            stringObjectHashMap.put("user", userService.findUserById(discussPost.getUserId()));
            list.add(stringObjectHashMap);
        }
        model.addAttribute("list", list);
        model.addAttribute("orderMode", orderMode);

        return "index";
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }


    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String profile(Model model,
                          @PathVariable("userId") int userId) {
        User holderUser = hostHolder.getUser();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("followeeCount", followService.getFolloweeCount(userId, 3));
        model.addAttribute("followerCount", followService.getFollowerCount(3, userId));
        if (holderUser == null) {
            model.addAttribute("haveFollow", false);
        } else {
            model.addAttribute("haveFollow", followService.whetherToFollow(holderUser.getId(), 3, userId));
        }
        model.addAttribute("User", user);
        model.addAttribute("likeCount", likeService.userLikeCount(userId));
        return "profile";
    }
}
