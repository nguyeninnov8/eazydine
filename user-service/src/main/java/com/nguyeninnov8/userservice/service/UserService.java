package com.nguyeninnov8.userservice.service;

import com.nguyeninnov8.userservice.dto.UserDto;
import com.nguyeninnov8.userservice.dto.UserRegistrationRequest;
import com.nguyeninnov8.userservice.dto.UserUpdateRequest;
import com.nguyeninnov8.userservice.event.UserEvent;
import com.nguyeninnov8.userservice.mapper.UserMapper;
import com.nguyeninnov8.userservice.model.User;
import com.nguyeninnov8.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserDto createUser(UserRegistrationRequest request) {
        // check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("User already exists");
        }

        // create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());
        user.setRoles(Set.of("USER"));
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalStateException("User not found"));
        return UserMapper.toUserDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
        return UserMapper.toUserDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalStateException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            user.setPhoneNumber(request.getEmail());
        }
        publishUserEvent(user, "UPDATED");
        userRepository.save(user);

        return UserMapper.toUserDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalStateException("User not found"));
        userRepository.delete(user);
        publishUserEvent(user, "DELETED");
    }

    private void publishUserEvent(User user, String eventType) {
        UserEvent userEvent = new UserEvent();
        userEvent.setUserId(user.getId());
        userEvent.setEmail(user.getEmail());
        userEvent.setRoles(user.getRoles());
        userEvent.setEventId(UUID.randomUUID().toString());
        userEvent.setEventTimestamp(LocalDateTime.now());
        userEvent.setEventType(eventType);

        kafkaTemplate.send("user-events", user.getId().toString(), userEvent);
    }
}
