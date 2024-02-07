package com.vecfonds.springsecurity.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "The username field can't be blank")
    private String username;
    @NotBlank(message = "The password field can't be blank")
    private String password;
}
