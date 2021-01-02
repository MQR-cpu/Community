package com.mqr.community.actuator;

import com.mqr.community.utils.CommunityConstant;
import com.mqr.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义actuator 端点
 */

@Endpoint(id = "database")
@Component
public class DatabaseEndPoint {

    @Resource
    private DataSource dataSource ;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndPoint.class);

    @ReadOperation
    public String database() {
        try (
                Connection connection = dataSource.getConnection();
                ){
            return CommunityUtil.getJSONString(0, "ok");

        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("数据库连接失败");
            return CommunityUtil.getJSONString(1, "false");
        }
    }

}
