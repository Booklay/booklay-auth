package com.nhnacademy.booklay.booklayauth.filter;

import static com.nhnacademy.booklay.booklayauth.filter.FilterUtils.addHeadersWhenAuthenticationSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.dto.reqeust.LoginRequest;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class OAuth2AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UUID_HEADER = "UUID";
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

            LoginRequest loginRequest =
                mapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginRequest.getMemberId(),
                                                        loginRequest.getPassword());

            return getAuthenticationManager().authenticate(token);

        } catch (Exception e) {
            log.error("????????? ????????? ??????: {}", e.getMessage());

            throw new IllegalAccessError("????????? ????????? ???????????????.");

        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws
        IOException, ServletException {

        addHeadersWhenAuthenticationSuccess(response, authResult, log, UUID_HEADER,
                                                                     REFRESH_TOKEN, redisTemplate);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        log.error("????????? ??????: {}", failed.toString());
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
