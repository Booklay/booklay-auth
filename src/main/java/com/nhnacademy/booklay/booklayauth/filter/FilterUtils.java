package com.nhnacademy.booklay.booklayauth.filter;

import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import com.nhnacademy.booklay.booklayauth.jwt.TokenUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterUtils {

    static void addHeadersWhenAuthenticationSuccess(HttpServletResponse response, Authentication authResult,
                                                    Logger log, String uuidHeader, String refreshToken2,
                                                    RedisTemplate<String, Object> redisTemplate) {

        CustomMember customMember = ((CustomMember) authResult.getPrincipal());
        String accessToken = TokenUtils.generateJwtToken(customMember);
        String uuid = TokenUtils.getUUIDFromToken(accessToken);
        String refreshToken = TokenUtils.generateRefreshToken(customMember);

        log.info("로구인 성공");
        response.addHeader(HttpHeaders.AUTHORIZATION, TokenUtils.BEARER + accessToken);
        response.addHeader(uuidHeader, uuid);
        response.addHeader(refreshToken2, refreshToken);
        response.addCookie(new Cookie("SESSION_ID", uuid));
        TokenUtils.saveJwtToRedis(redisTemplate, refreshToken, uuid);
    }

}
