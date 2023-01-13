package com.nhnacademy.booklay.booklayauth.dto.reqeust;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private String password;
}
