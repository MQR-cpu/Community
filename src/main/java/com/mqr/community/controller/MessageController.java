package com.mqr.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.mqr.community.entity.*;
import com.mqr.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/message/list",method = RequestMethod.GET)
    public String messageList(Model model, PageBean pageBean) {

        pageBean.setLimit(5);
        pageBean.setPath("/message/list");
        User user = hostHolder.getUser();
        pageBean.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversations = messageService.findConversations(user.getId(), pageBean.getOffset(), pageBean.getLimit());
        List<ViewObject> vos = new ArrayList<>();
        if (conversations != null) {
            for (Message message : conversations) {
                ViewObject vo = new ViewObject();
                vo.setViewObject("message",message);
                vo.setViewObject("toUser",userService.findUserById(message.getFromId() == user.getId() ? message.getToId() :message.getFromId()));
                vo.setViewObject("letterCount",messageService.findLetterCount(message.getConversationId()));
                vo.setViewObject("letterUnreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                vos.add(vo);
            }
        }

        model.addAttribute("messageVos",vos);
        //未读数量（私信，系统）
        model.addAttribute("userLetterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("noticeUnreadCountSum", messageService.findNoticeUnreadCount(user.getId(), null));


        return "letter";
    }

    @RequestMapping(value = "/letter/detail/{conversationId}/{cur}",method = RequestMethod.GET)
    public String letterDetail(Model model,
                               PageBean pageBean,
                               @PathVariable("conversationId") String conversationId,
                               @PathVariable(value = "cur") int cur) {
        pageBean.setLimit(5);
        pageBean.setPath("/letter/detail/"+conversationId +"/" + cur);
        User user = hostHolder.getUser();
        pageBean.setRows(messageService.findLetterCount(conversationId));



        List<ViewObject> vos = new ArrayList<>();
        List<Message> letters = messageService.findLetters(conversationId, pageBean.getOffset(), pageBean.getLimit());
        if (letters != null) {
            for (Message letter : letters) {
                ViewObject vo = new ViewObject();
                vo.setViewObject("letter",letter);
                vo.setViewObject("fromUser",userService.findUserById(letter.getFromId()));
                vos.add(vo);
            }
        }
        //修改消息 未读 已读 状态
        List<Integer> ids = helper(letters);
        if (!ids.isEmpty()) {
            messageService.updateStatus(ids,1);
        }

        String[] s = conversationId.split("_");
        String s1 = String.valueOf(user.getId());
        model.addAttribute("otherUser",userService.findUserById(s[0].equals(s1) ? Integer.parseInt(s[1]) : Integer.parseInt(s[0])));
        model.addAttribute("letterVos", vos);

        model.addAttribute("cur", cur);

        return "letter-detail";
    }

    /**
     * 未读私信id集合
     * @param list
     * @return
     */
    private List<Integer> helper(List<Message> list) {
        List<Integer> res = new ArrayList<>();
        if (list != null) {
            for (Message message : list) {
                if (message.getFromId() != hostHolder.getUser().getId() && message.getStatus() == 0) {
                    res.add(message.getId());
                }
            }
        }
        return res;
    }

    @RequestMapping(value = "/message/add",method = RequestMethod.POST)
    @ResponseBody
    public String addMessage(String toName,String content) {
        User toUser = userService.findUserByName(toName);
        if (toUser == null) {
            return CommunityUtil.getJSONString(1, "发送失败");
        }
        Message message = new Message();
        message.setToId(toUser.getId());
        message.setFromId(hostHolder.getUser().getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        String conversationId = "";
        if (toUser.getId() > hostHolder.getUser().getId()) {
            conversationId = hostHolder.getUser().getId() + "_" + toUser.getId();
        } else {
            conversationId = toUser.getId() + "_" + hostHolder.getUser().getId();
        }
        message.setConversationId(conversationId);


        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0, "发送成功");
    }

    @RequestMapping(value = "/notice/list",method = RequestMethod.GET)
    public String noticeList(Model model) {
        User user = hostHolder.getUser();

        //评论的通知
        Message message = messageService.findLatestNotice(user.getId(), EVENT_COMMENT);
        ViewObject messageVo = new ViewObject();
        if (message != null) {
            messageVo.setViewObject("message",message);
            String s = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> map = JSONObject.parseObject(s, Map.class);
            messageVo.setViewObject("user",userService.findUserById((Integer) map.get("userId")));
            messageVo.setViewObject("entityType",map.get("entityType"));
            messageVo.setViewObject("entityId",map.get("entityId"));
            messageVo.setViewObject("postId",map.get("postId"));
            messageVo.setViewObject("noticeCount",messageService.findNoticeCount(user.getId(),EVENT_COMMENT));
            messageVo.setViewObject("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), EVENT_COMMENT));

            model.addAttribute("messageVo", messageVo);
        }

        //点赞的通知
        message = messageService.findLatestNotice(user.getId(), EVENT_LIKE);
        ViewObject likeVo = new ViewObject();
        if (message != null) {
            likeVo.setViewObject("message",message);
            String s = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> map = JSONObject.parseObject(s, Map.class);
            likeVo.setViewObject("user",userService.findUserById((Integer) map.get("userId")));
            likeVo.setViewObject("entityType",map.get("entityType"));
            likeVo.setViewObject("entityId",map.get("entityId"));
            likeVo.setViewObject("postId",map.get("postId"));
            likeVo.setViewObject("noticeCount",messageService.findNoticeCount(user.getId(),EVENT_LIKE));
            likeVo.setViewObject("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), EVENT_LIKE));

            model.addAttribute("likeVo", likeVo);
        }

        //关注的通知
        message = messageService.findLatestNotice(user.getId(), EVENT_FOLLOW);
        ViewObject followVo = new ViewObject();
        if (message != null) {
            followVo.setViewObject("message",message);
            String s = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> map = JSONObject.parseObject(s, Map.class);
            followVo.setViewObject("user",userService.findUserById((Integer) map.get("userId")));
            followVo.setViewObject("entityType",map.get("entityType"));
            followVo.setViewObject("entityId",map.get("entityId"));
            followVo.setViewObject("postId",map.get("postId"));
            followVo.setViewObject("noticeCount",messageService.findNoticeCount(user.getId(),EVENT_FOLLOW));
            followVo.setViewObject("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), EVENT_FOLLOW));

            model.addAttribute("followVo", followVo);
        }
        System.out.println(followVo.getMap().get("noticeUnreadCount"));
        //未读的消息数量
        model.addAttribute("noticeUnreadCountSum", messageService.findNoticeUnreadCount(user.getId(), null));
        model.addAttribute("userLetterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));


        return "notice";
    }


    @RequestMapping(value = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String noticeList(Model model,
                             @PathVariable("topic") String topic,
                             PageBean pageBean) {
        User user = hostHolder.getUser();

        pageBean.setLimit(5);
        pageBean.setPath("/notice/detail/" + topic);
        pageBean.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNoticeList(user.getId(), topic, pageBean.getOffset(), pageBean.getLimit());
        List<ViewObject> vos = new ArrayList<>();
        if (noticeList != null) {
            for (Message message : noticeList) {
                ViewObject vo = new ViewObject();
                vo.setViewObject("message",message);
                String s = HtmlUtils.htmlUnescape(message.getContent());
                Map<String,Object> map = JSONObject.parseObject(s, Map.class);
                vo.setViewObject("user", userService.findUserById((Integer) map.get("userId")));
                vo.setViewObject("entityType",map.get("entityType"));
                vo.setViewObject("entityId",map.get("entityId"));
                vo.setViewObject("postId",map.get("postId"));
                //通知列表
                vo.setViewObject("fromUser", userService.findUserById(1));

                vos.add(vo);
            }
        }

        //设置已读
        //修改消息 未读 已读 状态
        List<Integer> ids = helper(noticeList);
        if (!ids.isEmpty()) {
            messageService.updateStatus(ids,1);
        }

        model.addAttribute("vos", vos);

        return "notice-detail";
    }

}
