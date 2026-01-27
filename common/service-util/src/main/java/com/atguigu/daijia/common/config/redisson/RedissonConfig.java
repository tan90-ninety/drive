package com.atguigu.daijia.common.config.redisson;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Data
@ConfigurationProperties("spring.data.redis")
public class RedissonConfig {
    private String host;
    private String port;
    private String password;
    private int timeout = 3000;
    private static String ADDRESS_PREFIX = "redis://";

    @Bean
    RedissonClient redisSingle() {
        Config config = new Config();
        if (!StringUtils.hasText(host)) {
            throw new RuntimeException("host is empty!");
        }
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(ADDRESS_PREFIX + host + ":" + port)
                .setTimeout(timeout);

        if (StringUtils.hasText(password)) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }

}
