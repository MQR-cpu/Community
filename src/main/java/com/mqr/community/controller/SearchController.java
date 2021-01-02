package com.mqr.community.controller;

import com.mqr.community.entity.DiscussPost;
import com.mqr.community.entity.PageBean;
import com.mqr.community.service.ElasticsearchService;
import com.mqr.community.service.LikeService;
import com.mqr.community.service.UserService;
import com.mqr.community.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;



    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, PageBean pageBean, Model model) throws IOException, ParseException {
        // 搜索帖子
        List<DiscussPost> searchResult = elasticsearchService.search(keyword, pageBean.getCurrentPage() - 1, pageBean.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.entityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        pageBean.setPath("/search?keyword=" + keyword);
        pageBean.setRows(elasticsearchService.getRows());

        return "search";
    }

}
