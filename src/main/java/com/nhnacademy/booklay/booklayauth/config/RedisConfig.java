package com.nhnacademy.booklay.booklayauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

/**
 * Redis 기본 설정을 위한 Configuration 클래스
 *
 * @author 조현진
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${booklay.redis.host}")
    private String host;

    @Value("${booklay.redis.port}")
    private int port;

    @Value("${booklay.redis.database}")
    private int database;

    @Value("${booklay.redis.password}")
    private String password;

    /**
     * Redis 연결과 관련된 설정을 하는 RedisConnectionFactory를 스프링 빈으로 등록한다.
     *
     * @return Thread-safe한 Lettuce 기반의 커넥션 팩토리 (LettuceConnectionFactory)
     * @author 조현진
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        var configuration = new RedisStandaloneConfiguration(host, port);

        configuration.setPassword(password);
        configuration.setDatabase(database);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

}
