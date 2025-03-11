package com.nguyeninnov8.authservice.service;

import com.nguyeninnov8.authservice.dto.LoginRequest;
import com.nguyeninnov8.authservice.dto.TokenResponse;
import com.nguyeninnov8.commonlib.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public TokenResponse login(LoginRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("username", request.getUsername());
            map.add("password", request.getPassword());

            log.info("Logging in with username: {}", request.getUsername());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, entity, TokenResponse.class);
            log.info("Login response: {}", response);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new ApiException("Failed to authenticate", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Error logging in", e);
            throw new ApiException("Error logging in");
        }
    }
}
