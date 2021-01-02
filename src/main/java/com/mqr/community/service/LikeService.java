package com.mqr.community.service;

import com.mqr.community.utils.CommunityUtil;
import com.mqr.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType, int entityId,int entityUserId) {
//        String key = RedisKeyUtil.getRedisKey(entityType, entityId);
//
//        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
//        if (isMember) {
//            redisTemplate.opsForSet().remove(key, userId);
//        } else {
//            redisTemplate.opsForSet().add(key, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getRedisKey(entityType, entityId);
                String entityUserLikeKey = RedisKeyUtil.getEntityUserRedisKey(entityUserId);

                // 查询语句不要放到redis事务中  redis会把事务中的语句放到队列中 ，事务提交时才会执行
                Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                redisOperations.multi();
                if (member) {
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(entityUserLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(entityUserLikeKey);
                }
                return redisOperations.exec();
            }
        });

    }

    public int userLikeCount(int userId) {
        String entityUserRedisKey = RedisKeyUtil.getEntityUserRedisKey(userId);
        Integer res = (Integer)redisTemplate.opsForValue().get(entityUserRedisKey);
        return res == null? 0 : res;
    }

    //查询实体点赞数量
    public long entityLikeCount(int entityType, int entityId) {
        String key = RedisKeyUtil.getRedisKey(entityType, entityId);
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0 : size;
    }

    //查询用户是否点赞状态 返回值用int 能表示多种状态  比如点踩
    public int userLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getRedisKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;
    }

}
