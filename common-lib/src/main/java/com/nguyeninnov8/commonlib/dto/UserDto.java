package com.nguyeninnov8.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    public String id;
    public String username;
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public List<String> roles;
}
