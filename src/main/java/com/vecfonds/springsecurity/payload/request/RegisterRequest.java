package com.vecfonds.springsecurity.payload.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}
