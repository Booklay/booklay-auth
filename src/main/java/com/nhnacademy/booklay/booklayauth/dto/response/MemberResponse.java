package com.nhnacademy.booklay.booklayauth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberResponse {

    private final String userId;

    private final String password;

    private final String authority;
}
