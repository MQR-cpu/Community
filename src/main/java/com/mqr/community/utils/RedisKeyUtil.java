package com.mqr.community.utils;

import javax.xml.crypto.Data;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uva";//unique visitor
    private static final String PREFIX_DAU = "daua";//daily activity user
    //热榜排行
    private static final String PREFIX_POST = "post";

    public static String getPostKey() {
        return PREFIX_POST + SPLIT + "scores";
    }

    //某个实体的点赞
    //like:entity:entityType:entityId
    public static String getRedisKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //用户得到的赞
    //like:user:entityUserId
    public static String getEntityUserRedisKey(int entityUserId) {
        return PREFIX_USER_LIKE + SPLIT + entityUserId;
    }

    //用户对实体的关注
    //followee:userId:entityType  zset(entityId,scores)
    public static String getFolloweeRedisKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT  + entityType;
    }

    //实体获得的关注
    //follower:entityType:entityId  zset(userId,scores)
    public static String getFollowerRedisKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //验证码
    public static String getKaptha(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId) {

        return PREFIX_USER + SPLIT + userId;
    }

    //一天的uv
    public static String getUvKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //某段时间的uv
    public static String getUvKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //一天的日活
    public static String getDauKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //某段时间的日活
    public static String getDauKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

}
