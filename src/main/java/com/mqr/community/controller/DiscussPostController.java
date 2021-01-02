package com.mqr.community.controller;

import com.mqr.community.dao.UserMapper;
import com.mqr.community.entity.*;
import com.mqr.community.event.EventProducer;
import com.mqr.community.service.CommentService;
import com.mqr.community.service.DiscussPostService;
import com.mqr.community.service.LikeService;
import com.mqr.community.service.UserService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import com.mqr.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Resource
    private DiscussPostService discussPostService;



    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPort(String title,String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你還沒有登陸呢");
        }

        if (StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(1, "标题或内容不能为空");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(EVENT_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());

        eventProducer.sendEvent(event);

        //将帖子id 加到缓存中  之后用定时器定时计算scores
        String key = RedisKeyUtil.getPostKey();
        redisTemplate.opsForSet().add(key, post.getId());

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

//    @RequestMapping(value = "/detail/{id}",method = RequestMethod.GET)
//    public String discussDetail(Model model ,@PathVariable("id") int id) {
//        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
//        if(discussPost == null){
//            return "redirect:/index";
//        }
//        model.addAttribute("discuss", discussPost);
//        model.addAttribute("discussPostUser", userMapper.selectById(discussPost.getUserId()));
//
//        return "discuss-detail";
//    }


    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, PageBean pageBean) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        if(post == null){
            return "redirect:/index";
        }
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("iuser", user);

        // 评论分页信息
        pageBean.setLimit(5);
        pageBean.setPath("/discuss/detail/" + discussPostId);
        pageBean.setRows(post.getCommentCount());

        model.addAttribute("postLikeCount", likeService.entityLikeCount(11, post.getId()));
        if (hostHolder.getUser() == null) {
            model.addAttribute("postLikeStatus", 0);
        } else {
            model.addAttribute("postLikeStatus", likeService.userLikeStatus(hostHolder.getUser().getId(),11, post.getId()));
        }

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), pageBean.getOffset(), pageBean.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("iuser", userService.findUserById(comment.getUserId()));
                //点赞数
                commentVo.put("commentLikeCount", likeService.entityLikeCount(2, comment.getId()));
                //点赞状态
                if (hostHolder.getUser() == null) {
                    commentVo.put("commentLikeStatus", 0);
                } else {
                    commentVo.put("commentLikeStatus", likeService.userLikeStatus(hostHolder.getUser().getId(), 2, comment.getId()));
                }

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        //点赞数
                        replyVo.put("replyLikeCount", likeService.entityLikeCount(2, reply.getId()));
                        if (hostHolder.getUser() == null) {
                            replyVo.put("replyLikeStatus", 0);
                        } else {
                            replyVo.put("replyLikeStatus", likeService.userLikeStatus(hostHolder.getUser().getId(),2, reply.getId()));
                        }

                        // 作者
                        replyVo.put("iuser", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "discuss-detail";
    }

    @RequestMapping(value = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String top(int id) {
        discussPostService.updateType(id,1);

        //触发发帖事件
        Event event = new Event()
                .setTopic(EVENT_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.sendEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(value = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String wonderful(int id) {
        discussPostService.updateStatus(id,1);

        //触发发帖事件
        Event event = new Event()
                .setTopic(EVENT_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.sendEvent(event);

        //将帖子id 加到缓存中  之后用定时器定时计算scores
        String key = RedisKeyUtil.getPostKey();
        redisTemplate.opsForSet().add(key, id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String delete(int id) {
        discussPostService.updateStatus(id,2);

        //触发删帖事件
        Event event = new Event()
                .setTopic(EVENT_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.sendEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
