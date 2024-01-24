package com.vecfonds.springsecurity.service;

import com.vecfonds.springsecurity.exception.TokenRefreshException;
import com.vecfonds.springsecurity.entity.RefreshToken;
import com.vecfonds.springsecurity.repository.RefreshTokenRepository;
import com.vecfonds.springsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${application.security.jwt.refresh-token}")
    private Long refreshTokenExpire;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken generateRefreshToken(Long userId){
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpireDate(Instant.now().plusMillis(refreshTokenExpire));

        refreshToken = refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpireDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new login request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId){
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }

    @Transactional
    public int deleteByUsername(String username){
        return refreshTokenRepository.deleteByUser(userRepository.findByUsername(username).get());
    }
}
