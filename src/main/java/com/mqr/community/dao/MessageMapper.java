package com.mqr.community.dao;

import com.mqr.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话返回一个最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //返回当前用户的会话数量
    int selectConversationCount(int userId);

    //查询私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //返回私信列表数量
    int selectLetterCount(String conversationId);

    //返回未读私信数量(用户未读 ，会话未读两种)
    int selectLetterUnreadCount(int userId, String conversationId);

    //添加私信
    int addMessage(Message message);

    //修改私信状态
    void updateStatus(List<Integer> ids,int status);

    //查询系统通知中最新的消息
    Message selectLatestNotice(int userId, String topic);

    //查询系统通知消息数量
    int selectNoticeCount(int userId, String topic);

    //查询未读系统通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询通知详情列表
    List<Message> selectNoticeList(int userId, String topic, int offset, int limit);
}
