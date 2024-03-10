package com.gasen.findmeetbackend.once;

import com.gasen.findmeetbackend.mapper.UserMapper;
import com.gasen.findmeetbackend.model.User;
import com.gasen.findmeetbackend.service.IUserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GASEN
 * @date 2024/3/10 20:32
 * @classType description
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private IUserService userService;

    /**
     * 10000条数据
     * for循环耗时：7.4073327s
     * 批量插入耗时：2.0207053s
     * 批量+异步耗时：1.0894986s
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<CompletableFuture<Void>> list = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            ArrayList<User> users = new ArrayList<>();
            for(int j = 0; j < 1000; j++) {
                User user = new User();
                user.setUserName("gasen");
                user.setUserAccount("gasen");
                user.setPassword("12345678");
                user.setAvatarUrl("mypicture.com");
                user.setGender(0);
                user.setEmail("gasen12345@163.com");
                user.setPhone("112354685");
                user.setTags("[]");
                users.add(user);
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(users, 1000);
            }, new ThreadPoolExecutor(20, 40, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000)));
            list.add(future);
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("插入用户耗时：" + stopWatch.getTotalTimeSeconds() + "s");
    }




}
