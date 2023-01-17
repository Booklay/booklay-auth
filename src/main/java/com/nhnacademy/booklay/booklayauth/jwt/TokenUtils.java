package com.nhnacademy.booklay.booklayauth.jwt;

import com.nhnacademy.booklay.booklayauth.constant.Roles;
import com.nhnacademy.booklay.booklayauth.dto.Member;
import io.jsonwebtoken.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    private static final String secretKey = "BOOKLAY_SECRET_KEY";

    public static String generateJwtToken(Member member) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(member.getEmail())
                .setHeader(createHeader())
                .setClaims(createClaims(member))
                .setExpiration(createExpireDateForOneYear())
                .signWith(SignatureAlgorithm.HS256, createSigningKey());

        return builder.compact();
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

    private static Date createExpireDateForOneYear() {
        // 토큰 만료시간은 30일으로 설정
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        return c.getTime();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    private static Map<String, Object> createClaims(Member member) {
        // 공개 클레임에 사용자의 이름과 이메일을 설정하여 정보를 조회할 수 있다.
        Map<String, Object> claims = new HashMap<>();

        claims.put("email", member.getEmail());
        claims.put("role", member.getRole());

        return claims;
    }

    private static Key createSigningKey() {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    private static Claims getClaimsFormToken(String token) {
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                .parseClaimsJws(token).getBody();
    }

    private static String getMemberEmailFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (String) claims.get("email");
    }

    private static Roles getRoleFromToken(String token) {
        Claims claims = getClaimsFormToken(token);
        return (Roles) claims.get("role");
    }
}
