package com.nguyeninnov8.authservice.service;

import com.nguyeninnov8.commonlib.dto.UserDto;
import com.nguyeninnov8.commonlib.exception.ApiException;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakService {
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
        // Validate that keycloak is not null at initialization time
        if (keycloak == null) {
            log.error("Keycloak client is null in constructor");
            throw new IllegalStateException("Keycloak client is null");
        }
        log.info("KeycloakService initialized with keycloak client");
    }

    public UserDto createUser(UserDto dto, String password) {
        try {
            // Create User Representation
            UserRepresentation user = getUserRepresentation(dto, password);

            // Create user in keycloak
            Response response = keycloak.realm(realm).users().create(user);

            if (response.getStatus() == 201) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                dto.setId(userId);

                // assign roles
                if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                    assignRolesToUser(userId, dto.getRoles());
                } else {
                    // assign default USER role
                    assignRolesToUser(userId, Collections.singletonList("USER"));
                    dto.setRoles(Collections.singletonList("USER"));
                }
                return dto;
            } else {
                log.error("Error creating user: {}", response.getStatusInfo());
                throw new ApiException("Error creating user");
            }
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw new ApiException("Error creating user");
        }
    }

    private UserRepresentation getUserRepresentation(UserDto dto, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(true);

        // Create credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        credentialRepresentation.setTemporary(false);
        user.setCredentials(Collections.singletonList(credentialRepresentation));
        return user;
    }

    public void assignRolesToUser(String userId, List<String> roles) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            List<RoleRepresentation> roleRepresentations = roles.stream()
                    .map(roleName -> keycloak.realm(realm).roles().get(roleName).toRepresentation())
                    .collect(Collectors.toList());

            userResource.roles().realmLevel().add(roleRepresentations);
        } catch (Exception e) {
            log.error("Error assigning roles to user", e);
            throw new ApiException("Error assigning roles to user");
        }
    }

    public UserDto getUserById(String userId) {
        try {
            UserRepresentation userPrep = keycloak.realm(realm).users().get(userId).toRepresentation();
            List<String> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listAll().stream()
                    .map(RoleRepresentation::getName)
                    .toList();

            UserDto userDto = new UserDto();
            userDto.setId(userPrep.getId());
            userDto.setUsername(userPrep.getUsername());
            userDto.setEmail(userPrep.getEmail());
            userDto.setFirstName(userPrep.getFirstName());
            userDto.setLastName(userPrep.getLastName());
            userDto.setRoles(roles);

            return userDto;
        } catch (Exception e) {
            log.error("Error getting user by id", e);
            throw new ApiException("Error getting user by id");
        }
    }
}
