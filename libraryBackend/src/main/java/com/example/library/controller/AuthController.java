package com.example.library.controller; // 根据你的包结构

import com.example.library.dto.LoginRequestDto; // 你需要创建这个 DTO
import com.example.library.dto.LoginResponseDto; // 你需要创建这个 DTO
import com.example.library.seurity.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/auth") // 或者你期望的 API 前缀
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // 你的 CustomUserDetailsService

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDto loginRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // throw new Exception("Incorrect username or password", e); // 或者返回自定义的错误响应
            return ResponseEntity.status(401).body("用户名或密码错误");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // 在 LoginResponseDto 中可以包含 token 和一些用户信息 (例如用户名、角色)
        return ResponseEntity.ok(new LoginResponseDto(jwt,
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.toList())));
    }
}

// 需要创建 LoginRequestDto.java:
// package com.example.library.dto;
// import lombok.Data;
// @Data public class LoginRequestDto { private String username; private String password; }

// 需要创建 LoginResponseDto.java:
// package com.example.library.dto;
// import lombok.AllArgsConstructor; import lombok.Data; import java.util.List;
// @Data @AllArgsConstructor public class LoginResponseDto { private String token; private String username; private List<String> roles; }