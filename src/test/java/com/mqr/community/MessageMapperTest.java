package com.mqr.community;

import com.mqr.community.dao.MessageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageMapperTest {

    @Resource
    private MessageMapper messageMapper;

    @Test
    public void test() {
        System.out.println(messageMapper.selectConversations(111,0, Integer.MAX_VALUE));
        System.out.println(messageMapper.selectConversationCount(111));
        System.out.println(messageMapper.selectLetters("111_112", 0, Integer.MAX_VALUE));

    }
}
