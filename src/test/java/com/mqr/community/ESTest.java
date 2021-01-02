package com.mqr.community;

import com.mqr.community.dao.DiscussPostMapper;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ESTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPortRepository discussPortRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testInsert() {
        discussPortRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPortRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPortRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

//    @Test
//    public void testInsertList() {
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(101, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(102, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(103, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(111, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(112, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(131, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(132, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(133, 0, 100));
//        discussPortRepository.saveAll(discussPostMapper.selectDiscussPorts(134, 0, 100));
//    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人,使劲灌水.");
        discussPortRepository.save(post);
    }

    @Test
    public void testDelete() {
        // discussPortRepository.deleteById(231);
        discussPortRepository.deleteAll();
    }



    @Test
    public void a() throws IOException, ParseException {
        SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"));
        searchSourceBuilder.sort(SortBuilders.fieldSort("type").order(SortOrder.DESC));
        searchSourceBuilder.sort(SortBuilders.fieldSort("score").order(SortOrder.DESC));
        searchSourceBuilder.sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        HighlightBuilder builder = new HighlightBuilder();
        builder.field("title").preTags("<em>").postTags("</em>");
        builder.field("content").preTags("<em>").postTags("</em>");

        searchSourceBuilder.highlighter(builder);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response.toString());


        SearchHits hits = response.getHits();
        TotalHits totalHits = hits.getTotalHits();
        System.out.println(totalHits.value);
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
            System.out.println(createTime);

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
            System.out.println(post);
        }



    }


//    @Test
//    public void testSearchByTemplate() {
//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withPageable(PageRequest.of(0, 10))
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
//
//        Page<DiscussPost> page = elasticsearchRestTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
//            @Override
//            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
//                SearchHits hits = response.getHits();
//                if (hits.getTotalHits() <= 0) {
//                    return null;
//                }
//
//                List<DiscussPost> list = new ArrayList<>();
//                for (SearchHit hit : hits) {
//                    DiscussPost post = new DiscussPost();
//
//                    String id = hit.getSourceAsMap().get("id").toString();
//                    post.setId(Integer.valueOf(id));
//
//                    String userId = hit.getSourceAsMap().get("userId").toString();
//                    post.setUserId(Integer.valueOf(userId));
//
//                    String title = hit.getSourceAsMap().get("title").toString();
//                    post.setTitle(title);
//
//                    String content = hit.getSourceAsMap().get("content").toString();
//                    post.setContent(content);
//
//                    String status = hit.getSourceAsMap().get("status").toString();
//                    post.setStatus(Integer.valueOf(status));
//
//                    String createTime = hit.getSourceAsMap().get("createTime").toString();
//                    post.setCreateTime(new Date(Long.valueOf(createTime)));
//
//                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
//                    post.setCommentCount(Integer.valueOf(commentCount));
//
//                    // 处理高亮显示的结果
//                    HighlightField titleField = hit.getHighlightFields().get("title");
//                    if (titleField != null) {
//                        post.setTitle(titleField.getFragments()[0].toString());
//                    }
//
//                    HighlightField contentField = hit.getHighlightFields().get("content");
//                    if (contentField != null) {
//                        post.setContent(contentField.getFragments()[0].toString());
//                    }
//
//                    list.add(post);
//                }
//
//                return new AggregatedPageImpl(list, pageable,
//                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
//            }
//        });
//
//        System.out.println(page.getTotalElements());
//        System.out.println(page.getTotalPages());
//        System.out.println(page.getNumber());
//        System.out.println(page.getSize());
//        for (DiscussPost post : page) {
//            System.out.println(post);
//        }
//    }

}
