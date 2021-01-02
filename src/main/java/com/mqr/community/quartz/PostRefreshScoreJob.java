package com.mqr.community.quartz;

import com.mqr.community.entity.DiscussPost;
import com.mqr.community.service.DiscussPostService;
import com.mqr.community.service.ElasticsearchService;
import com.mqr.community.service.LikeService;
import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.RedisKeyUtil;
import org.apache.kafka.common.requests.EpochEndOffset;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostRefreshScoreJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostRefreshScoreJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    //纪元
    private static final Date EPOCH;

    static {
        try {
            EPOCH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-6-6 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("初始化纪元失败");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String key = RedisKeyUtil.getPostKey();
        BoundSetOperations boundSetOperations = redisTemplate.boundSetOps(key);

        if (boundSetOperations.size() == 0) {
            logger.info("没有帖子要计算分数");
        }

        //开始刷新帖子分数
        logger.info("开始刷新帖子分数");
        while (boundSetOperations.size() > 0) {
            this.refreshPostScore((Integer) boundSetOperations.pop());
        }
        //刷新帖子结束
        logger.info("刷新帖子结束");
    }

    public void refreshPostScore(int id) {
        DiscussPost post = discussPostService.findDiscussPostById(id);
        if (post == null) {
            return ;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, id);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - EPOCH.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(id, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
