package com.nhnacademy.booklay.booklayauth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import com.nhnacademy.booklay.booklayauth.dto.reqeust.OAuth2LoginRequest;
import com.nhnacademy.booklay.booklayauth.jwt.TokenUtils;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class OAuth2AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UUID_HEADER = "X-User-UUID";
    private static final String REFRESH_TOKEN = "Refresh-Token";

    public OAuth2AuthenticationFilter(AuthenticationManager authenticationManager,
                                      ObjectMapper mapper,
                                      RedisTemplate<String, Object> redisTemplate) {
        super(authenticationManager);
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
        throws AuthenticationException {

        try {
            log.info("start Oauth2 login");
            log.info("uri = {}", request.getRequestURI());

            OAuth2LoginRequest oAuth2LoginRequest =
                mapper.readValue(request.getInputStream(), OAuth2LoginRequest.class);

            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(oAuth2LoginRequest.getEmail(),
                                                        oAuth2LoginRequest.getIdentity());

            return getAuthenticationManager().authenticate(token);

        } catch (Exception e) {
            log.error("잘못된 로그인 요청: {}", e.getMessage());

            throw new IllegalAccessError("잘못된 로그인 요청입니다.");

        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws
        IOException, ServletException {

        CustomMember customMember = ((CustomMember) authResult.getPrincipal());
        String accessToken = TokenUtils.generateJwtToken(customMember);
        String uuid = TokenUtils.getUUIDFromToken(accessToken);
        String refreshToken = TokenUtils.generateRefreshToken(customMember);

        log.info("로구인 성공");
        response.addHeader(HttpHeaders.AUTHORIZATION, TokenUtils.BEARER + accessToken);
        response.addHeader(UUID_HEADER, uuid);
        response.addHeader(REFRESH_TOKEN, refreshToken);
        response.addCookie(new Cookie("SESSION_ID", uuid));
        TokenUtils.saveJwtToRedis(redisTemplate, refreshToken, uuid);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        log.error("로그인 실패: {}", failed.toString());
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
