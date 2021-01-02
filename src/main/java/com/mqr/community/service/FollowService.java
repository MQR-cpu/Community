package com.mqr.community.service;

import com.mqr.community.entity.User;
import com.mqr.community.entity.ViewObject;
import com.mqr.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
                String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().add(followeeRedisKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerRedisKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unFollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
                String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().remove(followeeRedisKey, entityId);
                operations.opsForZSet().remove(followerRedisKey, userId);

                return operations.exec();
            }
        });
    }

    public long getFolloweeCount(int userId, int entityType) {
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
        // redis 查询不到值会返回null 用long的包装类Long 来接受
        Long aLong = redisTemplate.opsForZSet().zCard(followeeRedisKey);
        return aLong == null ? 0 : aLong;
    }

    public long getFollowerCount(int entityType, int entityId) {
        String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType, entityId);
        Long aLong = redisTemplate.opsForZSet().zCard(followerRedisKey);
        return aLong == null ? 0 : aLong;
    }

    public boolean whetherToFollow(int userId, int entityType, int entityId) {
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeRedisKey, entityId)!= null ? true : false;
    }


    //返回关注的人列表
    public List<ViewObject> getFolloweeList(int userId, int offset, int limit) {
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, 3);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followeeRedisKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        System.out.println(set.size() + "set");
        List<ViewObject> list = new ArrayList<>();
        for ( int tmp : set) {
            ViewObject vo = new ViewObject();
            User userById = userService.findUserById(tmp);
            vo.setViewObject("user",userById);
            Double score = redisTemplate.opsForZSet().score(followeeRedisKey, tmp);
            vo.setViewObject("date",new Date(score.longValue()));
            list.add(vo);
        }
        return list;
    }

    //返回粉丝列表
    public List<ViewObject> getFollowerList(int userId, int offset, int limit) {
        String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(3,userId);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followerRedisKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<ViewObject> list = new ArrayList<>();
        for ( int tmp : set) {
            ViewObject vo = new ViewObject();
            User userById = userService.findUserById(tmp);
            vo.setViewObject("user",userById);
            Double score = redisTemplate.opsForZSet().score(followerRedisKey, tmp);
            vo.setViewObject("date",new Date(score.longValue()));
            list.add(vo);
        }
        return list;
    }
}
