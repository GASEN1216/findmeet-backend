package com.gasen.findmeetbackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置类
 * @author GASEN
 * @date 2024/3/11 21:48
 * @classType description
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        //  使用单个Redis，没有开集群 useClusterServers()  设置地址和使用库
        config.useSingleServer().setAddress(redisAddress).setDatabase(0);
        // 2. 创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}