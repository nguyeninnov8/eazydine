package com.nguyeninnov8.authservice.client;
import com.nguyeninnov8.authservice.dto.RegisterRequest;
import com.nguyeninnov8.authservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PostMapping("/api/users")
    UserDto createUser(@RequestBody RegisterRequest userRegistrationRequest);

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);

}
