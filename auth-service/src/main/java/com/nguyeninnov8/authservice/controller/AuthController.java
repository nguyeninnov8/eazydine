package com.nguyeninnov8.authservice.controller;

import com.nguyeninnov8.authservice.dto.*;
import com.nguyeninnov8.authservice.service.AuthService;
import com.nguyeninnov8.commonlib.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authService.authenticate(authenticationRequest));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> refresh(@RequestBody TokenResponse tokenResponse) {
        return ResponseEntity.ok(authService.validateToken(tokenResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest tokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(tokenRequest));
    }

}
