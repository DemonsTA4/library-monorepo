package com.example.library.service.impl;

import com.example.library.dto.UserDto;
import com.example.library.entity.impl.User; // 假设这是你的 User 实体类
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 引入 PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 取消注释

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) { // 取消注释并添加 PasswordEncoder 参数
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // 取消注释
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto, "password");
        return userDto;
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username " + userDto.getUsername() + " already exists.");
        }
        User user = convertToEntity(userDto);
        user.setId(null); // 确保是新用户
        // 使用 PasswordEncoder 加密密码
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // 取消注释并使用
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userDto.getUsername() != null && !userDto.getUsername().equals(existingUser.getUsername())) {
                        if (userRepository.findByUsername(userDto.getUsername()).filter(u -> !u.getId().equals(id)).isPresent()) {
                            throw new IllegalArgumentException("Another user with username " + userDto.getUsername() + " already exists.");
                        }
                        existingUser.setUsername(userDto.getUsername());
                    }
                    // 只有当 DTO 中提供了新密码时才更新
                    if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword())); // 使用 PasswordEncoder 加密密码
                    }
                    if (userDto.getRole() != null) {
                        existingUser.setRole(userDto.getRole());
                    }
                    return convertToDto(userRepository.save(existingUser));
                });
    }

    @Override
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}