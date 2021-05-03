package com.tungpv.wallet.controller;

import com.tungpv.wallet.dto.request.LoginDto;
import com.tungpv.wallet.dto.request.RefreshTokenDto;
import com.tungpv.wallet.dto.request.SignUpDto;
import com.tungpv.wallet.dto.request.VerifyEmailDto;
import com.tungpv.wallet.dto.response.JwtResponse;
import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.security.jwt.JwtProvider;
import com.tungpv.wallet.security.services.UserPrinciple;
import com.tungpv.wallet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
@Valid
public class AuthRestAPIs {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        String jwtRefresh = jwtProvider.generateJwtRefreshToken(loginRequest.getEmail());
        return ResponseEntity.ok(new JwtResponse(jwt, jwtRefresh));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpDto signUpRequest) {
        userService.createUser(signUpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDto refreshToken){
        if (!jwtProvider.validateJwtToken(refreshToken.getRefreshToken())){
            throw new BadRequestException("Refresh token is invalid!");
        }
        String username = jwtProvider.getUserNameFromJwtToken(refreshToken.getRefreshToken());
        String jwt = jwtProvider.renewAccessToken(username);
        String jwtRefresh = jwtProvider.generateJwtRefreshToken(username);
        return ResponseEntity.ok(new JwtResponse(jwt, jwtRefresh));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        userService.verifyEmail(verifyEmailDto);
        return ResponseEntity.noContent().build();
    }
}
