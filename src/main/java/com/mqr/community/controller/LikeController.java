package com.mqr.community.controller;

import com.mqr.community.annotation.LoginRequired;
import com.mqr.community.entity.Event;
import com.mqr.community.entity.HostHolder;
import com.mqr.community.entity.User;
import com.mqr.community.event.EventProducer;
import com.mqr.community.service.LikeService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import com.mqr.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId ,int postId) {
        User user = hostHolder.getUser();
        System.out.println(user);
        if (user == null) {
            return CommunityUtil.getJSONString(1, "请登录");
        }
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        long count = likeService.entityLikeCount(entityType, entityId);
        int status = likeService.userLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        map.put("status", status);

        //触发点赞事件
        //只有点赞的时候发送事件  status == 1
        if (status == 1) {
            Event event = new Event()
                    .setTopic(EVENT_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.sendEvent(event);
        }

        //将帖子id 加到缓存中  之后用定时器定时计算scores
        if (entityType == ENTITY_TYPE_POST) {
            String key = RedisKeyUtil.getPostKey();
            redisTemplate.opsForSet().add(key, entityId);
        }

        return CommunityUtil.getJSONString(0, "成功", map);
    }
}
