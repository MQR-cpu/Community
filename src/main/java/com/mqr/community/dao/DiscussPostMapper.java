package com.mqr.community.dao;

import com.mqr.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;


@Mapper
@Repository
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPorts(@Param("userId") int userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit,
                                         int orderMode);

    int selectDiscussPortCounts(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
