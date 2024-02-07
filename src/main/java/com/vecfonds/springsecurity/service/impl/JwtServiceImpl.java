package com.vecfonds.springsecurity.service.impl;

import com.vecfonds.springsecurity.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtServiceImpl implements JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;


//    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    private Key keyz() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        Date currentDate=new Date();
        Date expireDate = new Date(currentDate.getTime()+jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(keyz(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyz())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(keyz())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect",ex.fillInStackTrace());
        }
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(keyz(), SignatureAlgorithm.HS512)
                .compact();
    }
}
