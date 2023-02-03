package com.nhnacademy.booklay.booklayauth.dto.reqeust;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuth2LoginRequest {

    private final String email;

    private final String identity;
}
