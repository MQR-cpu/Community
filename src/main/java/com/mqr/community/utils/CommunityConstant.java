package com.mqr.community.utils;

public interface CommunityConstant {
     String domain = "http://localhost:8080";
     String contextPath = "/";

     /**
      * 默认状态的登录凭证的超时时间
      */
     int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

     /**
      * 记住状态的登录凭证超时时间
      */
     int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

     /**
      * 激活成功
      */
     int ACTIVATION_SUCCESS = 2;

     /**
      * 重复激活
      */
     int ACTIVATION_REPEAT = 1;

     /**
      * 激活失败
      */
     int ACTIVATION_FAILURE = 3;

     /**
      * 用户不存在导致激活出错
      */

     int ACTIVATION_ERROR  = 0;


     /**
      * 帖子的评论
      */
     int ENTITY_TYPE_POST = 1;


     /**
      * 帖子的回复
      */
     int ENTITY_TYPE_COMMENT = 2;

     /**
      * 评论事件
      */
     String EVENT_COMMENT = "comment";

     /**
      * 点赞事件
      */
     String EVENT_LIKE = "like";

     /**
      * 关注事件
      */
     String EVENT_FOLLOW = "follow";

     /**
      * 发帖事件
      */
     String EVENT_PUBLISH = "publish";

     /**
      * 删帖事件
      */
     String EVENT_DELETE = "delete";

     /**
      * 系统用户
      */
     int SYSTEM_USER_ID = 1;

     /**
      * 权限：普通用户
      */
     String AUTHORITY_USER = "user";

     /**
      * 权限：admin
      */
     String AUTHORITY_ADMIN = "admin";

     /**
      * 权限：版主
      */
     String AUTHORITY_MODERATOR = "moderator";
}
