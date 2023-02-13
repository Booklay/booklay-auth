package com.nhnacademy.booklay.booklayauth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.filter.FormAuthenticationFilter;
import com.nhnacademy.booklay.booklayauth.filter.OAuth2AuthenticationFilter;
import com.nhnacademy.booklay.booklayauth.filter.RefreshAccessTokenFilter;
import com.nhnacademy.booklay.booklayauth.handler.CustomAccessDeniedHandler;
import com.nhnacademy.booklay.booklayauth.handler.CustomAuthenticationEntryPoint;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;

/**
 * Spring Security 기본 설정을 관리합니다.
 *
 * @author 조현진
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

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

        http.addFilterAfter(getAuthenticationFilter(null, null), ExceptionTranslationFilter.class)
            .addFilterAfter(getOAuth2AuthenticationFilter(null, null), FormAuthenticationFilter.class)
            .addFilterBefore(refreshAccessTokenFilter(null, null), OAuth2AuthenticationFilter.class);

        http.exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint());

        return http.build();

    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .antMatchers("/swagger*", "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs")
                .antMatchers("/h2-console/**");
    }

    @Bean
    public FormAuthenticationFilter getAuthenticationFilter(ObjectMapper mapper, RedisTemplate<String, Object> redisTemplate) throws Exception {
        FormAuthenticationFilter
            formAuthenticationFilter = new FormAuthenticationFilter(authenticationManager(null), mapper, redisTemplate);

        formAuthenticationFilter.setFilterProcessesUrl("/members/login");

        return formAuthenticationFilter;
    }

    @Bean
    public OAuth2AuthenticationFilter getOAuth2AuthenticationFilter(ObjectMapper mapper, RedisTemplate<String, Object> redisTemplate) throws Exception {

        OAuth2AuthenticationFilter oAuth2AuthenticationFilter =
            new OAuth2AuthenticationFilter(authenticationManager(null), mapper, redisTemplate);

        oAuth2AuthenticationFilter.setFilterProcessesUrl("/members/login/oauth2/code/github");

        return oAuth2AuthenticationFilter;
    }

    @Bean
    public RefreshAccessTokenFilter refreshAccessTokenFilter(ObjectMapper mapper, RedisTemplate<String,Object> redisTemplate) {
        return new RefreshAccessTokenFilter(mapper, redisTemplate);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public ExceptionTranslationFilter exceptionTranslationFilter(AuthenticationEntryPoint entryPoint) {
        return new ExceptionTranslationFilter(entryPoint);
    }
}
