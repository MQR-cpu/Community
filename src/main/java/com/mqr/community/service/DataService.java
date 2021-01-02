package com.mqr.community.service;

import com.mqr.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sf = new SimpleDateFormat("yyMMdd");

    /**
     * 添加UV
     * @param ip
     */
    public void addUv(String ip) {
        String uvKey = RedisKeyUtil.getUvKey(sf.format(new Date()));

        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    /**
     * 查询一段时间的UV
     * @param startDate
     * @param endDate
     * @return
     */
    public long findUvData(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("日期错误");
        }

        List<String> keyList = new ArrayList<>();
        Calendar calender = Calendar.getInstance();
        calender.setTime(startDate);
        while (!calender.getTime().after(endDate)) {
            String uvKey = RedisKeyUtil.getUvKey(sf.format(calender.getTime()));
            keyList.add(uvKey);
            calender.add(Calendar.DAY_OF_MONTH,1);
        }

        String unionKey = RedisKeyUtil.getUvKey(sf.format(startDate), sf.format(endDate));
         redisTemplate.opsForHyperLogLog().union(unionKey, keyList.toArray());
        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }

    /**
     * 添加日活
     * @param id
     */
    public void addDau(int id) {
        String dauKey = RedisKeyUtil.getDauKey(sf.format(new Date()));

        redisTemplate.opsForValue().setBit(dauKey, id, true);
    }

    public long findDau(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDauKey(sf.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDauKey(sf.format(start), sf.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }

}
