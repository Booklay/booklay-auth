package com.nhnacademy.booklay.booklayauth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import com.nhnacademy.booklay.booklayauth.dto.reqeust.LoginRequest;
import com.nhnacademy.booklay.booklayauth.jwt.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

/**
 * 스프링 시큐리티에 적용할 필터를 관리합니다.
 *
 * @author 조현진
 */
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UUID_HEADER = "X-User-UUID";
    public AuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper mapper, RedisTemplate<String, Object> redisTemplate) {
        super(authenticationManager);
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            log.info("start login");
            log.info("uri = {}", request.getRequestURI());

            LoginRequest loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(loginRequest.getMemberId(), loginRequest.getPassword());

            return getAuthenticationManager().authenticate(token);

        } catch (IOException e) {
            log.error("잘못된 로그인 요청: {}", e.getMessage());

            throw new IllegalAccessError("잘못된 로그인 요청입니다.");

        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        CustomMember customMember = ((CustomMember) authResult.getPrincipal());
        String token = TokenUtils.generateJwtToken(customMember);
        String uuid = TokenUtils.getUUIDFromToken(token);

        log.info("로구인 성공");
        response.addHeader(HttpHeaders.AUTHORIZATION, TokenUtils.BEARER + token);
        response.addHeader(UUID_HEADER, uuid);
        TokenUtils.saveJwtToRedis(redisTemplate, token, uuid);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        log.error("로그인 실패: {}", failed.toString());
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}

