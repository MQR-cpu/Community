package com.mqr.community.controller;

import com.mqr.community.entity.*;
import com.mqr.community.event.EventProducer;
import com.mqr.community.service.FollowService;
import com.mqr.community.service.UserService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow( int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(EVENT_FOLLOW)
                .setEntityId(entityId)
                .setEntityId(entityId)
                .setEntityUserId(entityId)
                .setUserId(user.getId());
        eventProducer.sendEvent(event);

        return CommunityUtil.getJSONString(0, "success");
    }

    @RequestMapping(value = "/unFollow",method = RequestMethod.POST)
    @ResponseBody
    public String unFollow( int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unFollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0, "success");
    }

    @RequestMapping(value = "/followee/{userId}",method = RequestMethod.GET)
    public String getFolloweeList(Model model,
                                  @PathVariable("userId") int userId,
                                  PageBean pageBean) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        pageBean.setLimit(5);
        pageBean.setPath("/followee/" + userId);
        pageBean.setRows((int)followService.getFolloweeCount(userId,3));

        List<ViewObject> vos = followService.getFolloweeList(userId, pageBean.getOffset(), pageBean.getLimit());
        User u  = hostHolder.getUser();
        if (vos != null) {
            for (ViewObject vo : vos) {
                if (u == null) {
                    vo.setViewObject("followStatus", false);
                } else {
                    User uu = (User)vo.getViewObject("user");
                    vo.setViewObject("followStatus",followService.whetherToFollow(u.getId(),3,uu.getId()));
                }
            }
        }
        System.out.println(vos.size());
        model.addAttribute("vos", vos);
        model.addAttribute("curUser", user);

        return "followee";

    }

    @RequestMapping(value = "/follower/{userId}",method = RequestMethod.GET)
    public String getFollowerList(Model model,
                                  @PathVariable("userId") int userId,
                                  PageBean pageBean) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        pageBean.setLimit(5);
        pageBean.setPath("/follower/" + userId);
        pageBean.setRows((int)followService.getFollowerCount(userId,3));

        List<ViewObject> vos = followService.getFollowerList(userId, pageBean.getOffset(), pageBean.getLimit());
        User u  = hostHolder.getUser();
        if (vos != null) {
            for (ViewObject vo : vos) {
                if (u == null) {
                    vo.setViewObject("followStatus", false);
                } else {
                    User uu = (User)vo.getViewObject("user");
                    vo.setViewObject("followStatus",followService.whetherToFollow(u.getId(),3,uu.getId()));
                }
            }
        }

        model.addAttribute("vos", vos);
        model.addAttribute("curUser", user);

        return "follower";

    }

}
