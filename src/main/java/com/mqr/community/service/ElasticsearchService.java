package com.mqr.community.service;

import com.mqr.community.dao.elasticsearch.DiscussPortRepository;
import com.mqr.community.entity.DiscussPost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPortRepository discussPortRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private  int  rows ;

    public int getRows() {
        return rows;
    }

    public void saveDiscussPost(DiscussPost post) {
        discussPortRepository.save(post);
    }

    public void deleteDiscussPort(DiscussPost post) {
        discussPortRepository.delete(post);
    }

    public List<DiscussPost> search(String keyword, int current, int limit) throws ParseException, IOException {
        SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "title", "content"));
        searchSourceBuilder.sort(SortBuilders.fieldSort("type").order(SortOrder.DESC));
        searchSourceBuilder.sort(SortBuilders.fieldSort("score").order(SortOrder.DESC));
        searchSourceBuilder.sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(limit);
        HighlightBuilder builder = new HighlightBuilder();
        builder.field("title").preTags("<em>").postTags("</em>");
        builder.field("content").preTags("<em>").postTags("</em>");

        searchSourceBuilder.highlighter(builder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response.toString());

        SearchHits hits = response.getHits();
        if (hits == null) {
            return null;
        }

        TotalHits totalHits = hits.getTotalHits();
        rows =(int)totalHits.value;

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : hits) {

            DiscussPost post = new DiscussPost();

            String id = hit.getSourceAsMap().get("id").toString();
            post.setId(Integer.valueOf(id));

            String userId = hit.getSourceAsMap().get("userId").toString();
            post.setUserId(Integer.valueOf(userId));

            String title = hit.getSourceAsMap().get("title").toString();
            post.setTitle(title);

            String content = hit.getSourceAsMap().get("content").toString();
            post.setContent(content);

            String status = hit.getSourceAsMap().get("status").toString();
            post.setStatus(Integer.valueOf(status));

            String createTime = hit.getSourceAsMap().get("createTime").toString();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            post.setCreateTime(simpleDateFormat.parse(createTime));


            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                post.setTitle(titleField.getFragments()[0].toString());
            }

            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }

            String commentCount = hit.getSourceAsMap().get("commentCount").toString();
            post.setCommentCount(Integer.valueOf(commentCount));
            list.add(post);

        }

        return list;
    }
}
