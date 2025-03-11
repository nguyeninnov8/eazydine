package com.nguyeninnov8.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    public Long id;
    public String username;
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public Set<String> roles;
}
