package com.nhnacademy.booklay.booklayauth.service;

import com.nhnacademy.booklay.booklayauth.domain.CustomMember;
import com.nhnacademy.booklay.booklayauth.dto.response.MemberResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${booklay.shop-origin}")
    private String url;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.contains("GIT_")) {
            MemberResponse memberResponse =
                restTemplate.getForObject(url + "members/login/?memberId=" + username,
                                          MemberResponse.class);


            isValidMemberResponse(username, memberResponse);



            return new CustomMember(memberResponse.getUserId(), memberResponse.getPassword(),
                                    Collections.singletonList(memberResponse.getAuthority()));

        }

        MemberResponse memberResponse =
            restTemplate.getForObject(url + "members/login/?memberId=" + username,
                                      MemberResponse.class);

        isValidMemberResponse(username, memberResponse);


        return new CustomMember(memberResponse.getEmail(), memberResponse.getPassword(),
                                Collections.singletonList(memberResponse.getAuthority()));
    }

    private static void isValidMemberResponse(String username, MemberResponse memberResponse) {
        if (memberResponse == null) {
            throw new UsernameNotFoundException(username);
        }
    }
}
