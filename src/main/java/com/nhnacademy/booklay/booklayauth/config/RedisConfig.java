package com.nhnacademy.booklay.booklayauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RestTemplate restTemplate;

}
