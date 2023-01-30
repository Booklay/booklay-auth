package com.nhnacademy.booklay.booklayauth.dto.reqeust;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class LoginRequest {

    @NotNull
    private String memberId;

    @NotNull
    private String password;
}
