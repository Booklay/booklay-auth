package com.nhnacademy.booklay.booklayauth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.filter.FormAuthenticationFilter;
import com.nhnacademy.booklay.booklayauth.filter.OAuth2AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 기본 설정을 관리합니다.
 *
 * @author 조현진
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests()
                .antMatchers("/**").permitAll();

        http.headers()
                .frameOptions().sameOrigin();

        http.addFilter(getAuthenticationFilter())
            .addFilterBefore(getOAuth2AuthenticationFilter(), FormAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .antMatchers("/swagger*", "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs")
                .antMatchers("/h2-console/**");
    }

    private FormAuthenticationFilter getAuthenticationFilter() throws Exception {
        FormAuthenticationFilter
            formAuthenticationFilter = new FormAuthenticationFilter(authenticationManager(null), mapper, redisTemplate);

        formAuthenticationFilter.setFilterProcessesUrl("/members/login");

        return formAuthenticationFilter;
    }

    private OAuth2AuthenticationFilter getOAuth2AuthenticationFilter() throws Exception {

        OAuth2AuthenticationFilter oAuth2AuthenticationFilter =
            new OAuth2AuthenticationFilter(authenticationManager(null), mapper, redisTemplate);

        oAuth2AuthenticationFilter.setFilterProcessesUrl("/members/login/oauth2/github");

        return oAuth2AuthenticationFilter;
    }
}
