package com.example.library.service;

import com.example.library.dto.UserDto; // You'll need a UserDto
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserDto userDto);
    Optional<UserDto> getUserById(Long id);
    Optional<UserDto> getUserByUsername(String username);
    List<UserDto> getAllUsers();
    Optional<UserDto> updateUser(Long id, UserDto userDto);
    boolean deleteUser(Long id);
}