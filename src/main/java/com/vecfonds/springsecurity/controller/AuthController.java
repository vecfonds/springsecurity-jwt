package com.vecfonds.springsecurity.controller;


import com.vecfonds.springsecurity.entity.RefreshToken;
import com.vecfonds.springsecurity.entity.Role;
import com.vecfonds.springsecurity.entity.User;
import com.vecfonds.springsecurity.exception.TokenRefreshException;
import com.vecfonds.springsecurity.payload.request.LoginRequest;
import com.vecfonds.springsecurity.payload.request.RegisterRequest;
import com.vecfonds.springsecurity.payload.request.TokenRefreshRequest;
import com.vecfonds.springsecurity.payload.response.JwtResponse;
import com.vecfonds.springsecurity.payload.response.MessageResponse;
import com.vecfonds.springsecurity.payload.response.TokenRefreshResponse;
import com.vecfonds.springsecurity.repository.RoleRepository;
import com.vecfonds.springsecurity.repository.UserRepository;
import com.vecfonds.springsecurity.service.JwtService;
import com.vecfonds.springsecurity.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtService.generateToken(authentication);

//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepository.findByUsername(request.getUsername()).get();

        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(currentUser.getId());


        return new ResponseEntity<>(new JwtResponse(token, refreshToken.getToken(), currentUser.getAuthorities()), HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            return new ResponseEntity<>(new MessageResponse("Username is taken!"), HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("User registered success!"), HttpStatus.OK);

    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request){
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateTokenFromUsername(user.getUsername());
                    return new ResponseEntity<>(new TokenRefreshResponse(token,requestRefreshToken), HttpStatus.OK);
                        }
                        )
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            String username = authentication.getName();
            refreshTokenService.deleteByUsername(username);

            return new ResponseEntity<>(new MessageResponse("Log out successful!"), HttpStatus.OK);
    }
}
