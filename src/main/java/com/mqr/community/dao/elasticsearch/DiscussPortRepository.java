package com.mqr.community.dao.elasticsearch;

import com.mqr.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPortRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
