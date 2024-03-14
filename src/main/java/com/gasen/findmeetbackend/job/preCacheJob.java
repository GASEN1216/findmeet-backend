package com.gasen.findmeetbackend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 * @author GASEN
 * @date 2024/3/11 12:27
 * @classType description
 */
@Component
@Slf4j
public class preCacheJob {


    @Resource
    private RedissonClient redissonClient;

    @Resource
    private IUserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    //要预热的用户id列表
    private List<Integer> userIds = Arrays.asList(1);

    @Scheduled(cron = "0 28 23 * * *")
    public void preCacheJob() {
        RLock lock = redissonClient.getLock("findmeet:preCacheJob:lock");
        try {
            if(lock.tryLock(0,-1, TimeUnit.MINUTES)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                ValueOperations valueOperations = redisTemplate.opsForValue();
                String redisKey = String.format("findmeet:user:recommend:%s:%s:%s",userIds.get(0),1,5);
                Page<User> userList = userService.page(new Page<>(1, 5),new QueryWrapper<>());
                log.info(LocalDateTime.now() +"缓存预热成功");
                valueOperations.set(redisKey, userList, 10, TimeUnit.MINUTES);
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            if(lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
