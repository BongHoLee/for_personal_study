package com.example.session.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-03-31
 */

@EnableRedisHttpSession
public class HttpSessionConfig extends AbstractHttpSessionApplicationInitializer {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        return new LettuceConnectionFactory();
    }

}
