package com.nguyeninnov8.authservice.controller;

import com.nguyeninnov8.authservice.dto.LoginRequest;
import com.nguyeninnov8.authservice.dto.RegisterRequest;
import com.nguyeninnov8.authservice.dto.TokenResponse;
import com.nguyeninnov8.authservice.service.AuthService;
import com.nguyeninnov8.authservice.service.KeycloakService;
import com.nguyeninnov8.commonlib.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.annotations.Body;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final KeycloakService keycloakService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        UserDto dto = new UserDto();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setRoles(request.getRoles());

        UserDto userDto = keycloakService.createUser(dto, request.getPassword());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        UserDto userDto = keycloakService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth service is working!");
    }
}
