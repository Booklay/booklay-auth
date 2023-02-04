package com.nhnacademy.booklay.booklayauth.dto.reqeust;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RefreshTokenRequest {

    private final String refreshToken;

    private final String accessToken;
}
