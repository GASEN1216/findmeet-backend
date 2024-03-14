package com.gasen.findmeetbackend;

import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.service.impl.UserServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
@Slf4j
class findmeetBackendApplicationTests {

    @Resource
    private UserServiceImpl userService;

    @Test
    void contextLoads() {
        List<String> tags = Arrays.asList("java", "c++", "Python");
        log.info(tags.toString());
        List<User> users = userService.selectByTags(tags);
        System.out.println(users);
    }

}
