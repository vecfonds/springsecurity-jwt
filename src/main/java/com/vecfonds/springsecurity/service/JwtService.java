package com.vecfonds.springsecurity.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {

    public String generateToken(Authentication authentication);
    public String getUsernameFromJWT(String token);

    public boolean validateToken(String token);

    public String generateTokenFromUsername(String username);
}
