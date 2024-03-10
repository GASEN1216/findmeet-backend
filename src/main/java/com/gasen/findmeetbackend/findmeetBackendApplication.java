package com.gasen.findmeetbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.gasen.findmeetbackend.mapper")
@EnableScheduling
@EnableRedisHttpSession
public class findmeetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(findmeetBackendApplication.class, args);
    }

}
