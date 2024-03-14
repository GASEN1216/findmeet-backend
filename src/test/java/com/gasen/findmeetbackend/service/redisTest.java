package com.gasen.findmeetbackend.service;

import com.gasen.findmeetbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * redis测试类
 * @author GASEN
 * @date 2024/3/11 8:56
 * @classType description
 */
@SpringBootTest
public class redisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("redis:user:gasen", "1", 10000, TimeUnit.SECONDS);
        Assert.assertEquals("1", valueOperations.get("redis:user:gasen"));
        redisTemplate.delete("redis:user:gasen");
        Assert.assertNull(valueOperations.get("redis:user:gasen"));
        User user = new User();
        user.setId(0);
        user.setUserName("");
        user.setUserAccount("");
        user.setPassword("");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setEmail("");
        user.setPhone("");
        user.setGrade(0);
        user.setExp(0);
        user.setState(0);
        user.setIsDelete(0);
        user.setTags("[\"java\"]");
        valueOperations.set("redis:user:gasen", user, 1000, TimeUnit.SECONDS);

    }


}
