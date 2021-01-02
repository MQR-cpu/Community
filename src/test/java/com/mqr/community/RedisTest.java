package com.mqr.community;

import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test() {
        redisTemplate.opsForValue().set("1", "mqr");
        System.out.println(redisTemplate.opsForValue().get("1"));
    }

    @Test
    public void afdsf() {
        Integer a = null;
        System.out.println(a);
    }
}
