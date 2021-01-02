package com.mqr.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mqr.community.entity.DiscussPost;
import com.mqr.community.entity.Event;
import com.mqr.community.entity.Message;
import com.mqr.community.service.DiscussPostService;
import com.mqr.community.service.ElasticsearchService;
import com.mqr.community.service.MessageService;
import com.mqr.community.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {EVENT_COMMENT, EVENT_FOLLOW, EVENT_LIKE})
    public void handleEvent(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if (event == null) {
            logger.error("事件为空");
            return;
        }

        //存到message表中
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());
        //message.content 为系统通知所需要的数据
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        //
        if (!event.getData().isEmpty()) {
            for (String key : event.getData().keySet()) {
                content.put(key, event.getData().get(key));
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = {EVENT_PUBLISH})
    public void handlePublishEvent(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if (event == null) {
            logger.error("事件为空");
            return;
        }

        DiscussPost discussPostById = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPostById);
    }

    @KafkaListener(topics = {EVENT_DELETE})
    public void handleDeleteEvent(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if (event == null) {
            logger.error("事件为空");
            return;
        }

        DiscussPost discussPostById = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.deleteDiscussPort(discussPostById);
    }

}
