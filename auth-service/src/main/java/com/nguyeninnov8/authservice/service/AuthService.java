package com.nguyeninnov8.authservice.service;


import com.nguyeninnov8.authservice.client.UserServiceClient;
import com.nguyeninnov8.authservice.dto.*;
import com.nguyeninnov8.authservice.event.UserEvent;
import com.nguyeninnov8.authservice.security.JwtService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(RegisterRequest request) {
        try {
            // check if user already exists
            try {
                UserDto existingUser = userServiceClient.getUserByEmail(request.getEmail());
                if (existingUser != null) {
                    throw new RuntimeException("User already exists");
                }
            } catch (FeignException.FeignClientException.NotFound e) {
                // User not found --> Happy case
            }

            // create user
            request.setPassword(passwordEncoder.encode(request.getPassword()));

            // Create user via user service
            UserDto user = userServiceClient.createUser(request);

            //Generate token
            String accessToken = jwtService.generateToken(user.getEmail(), user.getId());
            String refreshToken = jwtService.generateToken(user.getEmail(), user.getId());

            // Publish user created event
            publishUserEvent(user);
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();

        } catch (Exception e) {
            log.error("Error registering user", e);
            throw new RuntimeException("Error registering user");
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            UserDto user = userServiceClient.getUserByEmail(request.getEmail());

            if (user == null) {
                throw new RuntimeException("User not found");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            //Generate token
            String accessToken = jwtService.generateToken(user.getEmail(), user.getId());
            String refreshToken = jwtService.generateToken(user.getEmail(), user.getId());

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
        } catch (FeignException.FeignClientException.NotFound e) {
            throw new RuntimeException("User not found");
        } catch (Exception e) {
            log.error("Error authenticating user", e);
            throw new RuntimeException("Error authenticating user");
        }
    }

    private void publishUserEvent(UserDto user) {
        // Publish user created event
        UserEvent event = new UserEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("REGISTERED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setRoles(user.getRoles());
        event.setEventTimestamp(LocalDateTime.now());

        kafkaTemplate.send("user-events", event.getEventId(), event);

        log.info("User event published: {}", event);

    }

    public Map<String, Boolean> validateToken(TokenResponse tokenResponse) {
        return jwtService.isTokenValid(tokenResponse.getAccessToken());
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest tokenRequest) {
        // verify refresh token
        boolean isRefreshTokenValid = jwtService.isTokenValid(tokenRequest.getRefreshToken()).get("valid");

        if (!isRefreshTokenValid) {
            throw new RuntimeException("Invalid refresh token");
        }

        // extract user details from refresh token
        String userIdStr = jwtService.extractUserId(tokenRequest.getRefreshToken());
        String email = jwtService.extractUsername(tokenRequest.getRefreshToken());
        Long userId = Long.parseLong(userIdStr);

        // get user details
        UserDto user = userServiceClient.getUserById(userId);

        //Generate token
        String accessToken = jwtService.generateToken(email, userId);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(tokenRequest.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
