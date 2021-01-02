package com.mqr.community;

import com.mqr.community.dao.DiscussPostMapper;
import com.mqr.community.entity.DiscussPost;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@org.springframework.boot.test.context.SpringBootTest
@RunWith(SpringRunner.class)
public class SpringBootTest {

    /**
     * 单元测试要具有独立性，以后功能修改方便测试,之后运行整个单元测试类 全是对勾 就表示通过
     *
     * @BeforeClass @AfterClass @Before @After
     */

    private DiscussPost data;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {

    }


    @Before
    public  void before() {

    }

    @After
    public  void after() {

    }

    @Test
    public void testInsert() {
        Assert.assertEquals("mqr", "mqr");
    }

}
