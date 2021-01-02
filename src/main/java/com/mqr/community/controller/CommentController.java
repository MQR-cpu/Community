package com.mqr.community.controller;

import com.mqr.community.entity.Comment;
import com.mqr.community.entity.DiscussPost;
import com.mqr.community.entity.Event;
import com.mqr.community.entity.HostHolder;
import com.mqr.community.event.EventProducer;
import com.mqr.community.service.CommentService;
import com.mqr.community.service.DiscussPostService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant  {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId,
                             Comment comment) {

        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());

        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(EVENT_COMMENT)
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setUserId(hostHolder.getUser().getId())
                .setData("postId", discussPostId);
        //event.entityUserId  1.帖子的作者  2. 评论的作者
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        } else {
            Comment commentById = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(commentById.getUserId());
        }

        eventProducer.sendEvent(event);

        //触发es发帖事件
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(EVENT_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_COMMENT)
                    .setEntityId(discussPostId);

            eventProducer.sendEvent(event);

            //将帖子id 加到缓存中  之后用定时器定时计算scores
            String key = RedisKeyUtil.getPostKey();
            redisTemplate.opsForSet().add(key, discussPostId);

        }

        return "redirect:/discuss/detail/"+discussPostId;
    }
}
