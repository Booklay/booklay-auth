package com.nhnacademy.booklay.booklayauth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.booklay.booklayauth.constant.Roles;
import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import com.nhnacademy.booklay.booklayauth.dto.reqeust.RefreshTokenRequest;
import com.nhnacademy.booklay.booklayauth.jwt.TokenUtils;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class RefreshAccessTokenFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final static String UUID_HEADER = "UUID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        RefreshTokenRequest refreshTokenRequest = mapper.readValue(request.getInputStream(), RefreshTokenRequest.class);

        if (TokenUtils.isValidToken(refreshTokenRequest.getRefreshToken()))  {
            String accessToken = refreshTokenRequest.getAccessToken();

            String email = TokenUtils.getMemberEmailFromToken(accessToken);
            Roles roles = TokenUtils.getRoleFromToken(accessToken);

            CustomMember customMember = new CustomMember(email, null, Collections.singletonList(roles));

            String newToken = TokenUtils.generateJwtToken(customMember);

            log.info("로구인 성공");
            response.addHeader(HttpHeaders.AUTHORIZATION, TokenUtils.BEARER + accessToken);

            String uuidFromToken = TokenUtils.getUUIDFromToken(newToken);
            response.addHeader(UUID_HEADER, uuidFromToken);
            response.addCookie(new Cookie("SESSION_ID", uuidFromToken));
            TokenUtils.saveJwtToRedis(redisTemplate, newToken, uuidFromToken);
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/members/refresh");
    }
}
