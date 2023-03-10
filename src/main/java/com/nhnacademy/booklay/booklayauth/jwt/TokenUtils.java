package com.nhnacademy.booklay.booklayauth.jwt;

import com.nhnacademy.booklay.booklayauth.constant.Roles;
import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import io.jsonwebtoken.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenUtils {

    public static final String SECRET_KEY = "cQfTjWmZq4t7w!z%C*F-JaNdRgUkXp2r5u8x/A?D(G+KbPeShVmYq3t6v9y$B&E)";
    public static final String BEARER = "Bearer ";
    public static final String TOKEN = "TOKEN";

    public static String generateJwtToken(CustomMember customMember) {
        JwtBuilder accessToken = Jwts.builder()
                .setSubject(customMember.getUsername())
                .setHeader(createHeader())
                .setClaims(createClaims(customMember))
                .setExpiration(createExpireDateForOneHour())
                .signWith(SignatureAlgorithm.HS256, createSigningKey());

        return accessToken.compact();
    }

    public static String generateRefreshToken(CustomMember customMember) {
        JwtBuilder refreshToken = Jwts.builder()
                                    .setSubject(customMember.getUsername())
                                    .setExpiration(createExpireDateForOneMonth())
                                    .signWith(SignatureAlgorithm.HS256, createSigningKey());

        return refreshToken.compact();
    }

    public static void saveJwtToRedis(RedisTemplate<String, Object> redisTemplate, String token, String uuid) {
        redisTemplate.opsForHash()
                .put(uuid, TOKEN, token);
    }

    public static boolean isValidToken(String token) {
        try {
            Claims claims = getClaimsFormToken(token);
            log.info("expireTime :" + claims.getExpiration());
            log.info("email :" + claims.get("email"));
            log.info("role :" + claims.get("role"));
            return true;

        } catch (ExpiredJwtException exception) {
            log.error("Token Expired");
            return false;
        } catch (JwtException exception) {
            log.error("Token Tampered");
            return false;
        } catch (NullPointerException exception) {
            log.error("Token is null");
            return false;
        }
    }

    public static String getTokenFromHeader(String header) {
        return header.split(" ")[1];
    }

    private static Date createExpireDateForOneMonth() {
        // ?????? ??????????????? 30????????? ??????
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        return c.getTime();
    }

    private static Date createExpireDateForOneHour() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        return c.getTime();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    private static Map<String, Object> createClaims(CustomMember customMember) {
        // ?????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ????????? ??? ??????.
        Map<String, Object> claims = new HashMap<>();

        claims.put("email", customMember.getUsername());
        claims.put("role", customMember.getAuthorities());
        claims.put("uuid", UUID.randomUUID().toString());

        return claims;
    }

    private static Key createSigningKey() {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    private static Claims getClaimsFormToken(String token) {
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .parseClaimsJws(token).getBody();
    }

    public static String getMemberEmailFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (String) claims.get("email");
    }

    public static Roles getRoleFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        var role =
            (ArrayList<LinkedHashMap<String, String>>) claims.get("role");

        String authority = role.get(0).get("authority");
        return Roles.valueOf(authority);
    }

    public static String getUUIDFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (String) claims.get("uuid");
    }

}
