package com.vecfonds.springsecurity.payload.response;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer ";

    private Collection<? extends GrantedAuthority> roles;

//    public AuthResponseDto(String accessToken, Collection<? extends GrantedAuthority> roles) {
//        this.accessToken = accessToken;
//        this.roles = roles;
//    }


    public JwtResponse(String accessToken, String refreshToken, Collection<? extends GrantedAuthority> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.roles = roles;
    }
}
