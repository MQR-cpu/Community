package com.mqr.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        //java 数据 存到redis里必须指定序列化方式

        //设置key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        //设置hash 的key序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //设置hash 的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
