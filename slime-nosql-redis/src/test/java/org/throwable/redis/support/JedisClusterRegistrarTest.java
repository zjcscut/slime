package org.throwable.redis.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.redis.Application;
import org.throwable.redis.annotation.EnableRedisCluster;
import org.throwable.redis.configuration.RedisProperties;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/23 20:28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@EnableRedisCluster
public class JedisClusterRegistrarTest {

    @Autowired(required = false)
    private RedisProperties redisProperties;

    @Test
    public void testProcess()throws Exception{
        System.out.println(redisProperties.getPassword());
    }

}