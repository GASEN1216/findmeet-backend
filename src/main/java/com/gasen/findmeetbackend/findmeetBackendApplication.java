package com.gasen.findmeetbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gasen.findmeetbackend.mapper")
public class findmeetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(findmeetBackendApplication.class, args);
    }

}
