package com.mqr.community;

import com.mqr.community.dao.DiscussPostMapper;
import com.mqr.community.dao.UserMapper;
import com.mqr.community.entity.DiscussPost;
import com.mqr.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserMapper {

    private static final Logger logger = LoggerFactory.getLogger(TestUserMapper.class);

    @Resource
    UserMapper userMapper;

    @Resource
    DiscussPostMapper discussPostMapper;

    @Test
    public void test() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPorts(149, 0, 10,0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        System.out.println(discussPostMapper.selectDiscussPortCounts(0));
    }

    @Test
    public void testLogger() {
        logger.error("logger  error");
        logger.debug("logger  debug");
        logger.info("logger info");
        logger.warn("logger warn");
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setSalt("1");
        user.setUsername("1");
        user.setPassword("1");
        user.setHeaderUrl("1");
        user.setType(1);
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setActivationCode("1");
        userMapper.insertUser(user);
    }



}
