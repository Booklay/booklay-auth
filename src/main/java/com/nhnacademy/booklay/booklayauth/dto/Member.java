package com.nhnacademy.booklay.booklayauth.dto;

import com.nhnacademy.booklay.booklayauth.constant.Roles;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {

    private String email;

    private String password;

    private Roles role;

}
