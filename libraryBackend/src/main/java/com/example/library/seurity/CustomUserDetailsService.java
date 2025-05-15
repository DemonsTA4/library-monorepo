package com.example.library.seurity; // 你可以根据你的项目结构调整包名

import com.example.library.entity.impl.User; // 你的 User 实体类
import com.example.library.repository.UserRepository; // 你的 UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("userDetailsService") // 将其声明为一个 Spring Service Bean
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 建议将只读操作标记为事务性只读
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        System.out.println(user);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(), // 如果你的 User 实体有 enabled 字段
                true,             // accountNonExpired
                true,             // credentialsNonExpired
                true,             // accountNonLocked
                getAuthorities(user.getRole()) // 获取用户的权限/角色
        );
    }

    // 将角色字符串转换为 GrantedAuthority 集合
    // 假设你的 User 实体中的 role 字段是单个角色字符串，如 "ROLE_USER" 或 "ROLE_ADMIN"
    // 如果你有多个角色，可能需要调整此逻辑（例如，角色以逗号分隔存储，或者有单独的角色实体和关联表）
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // 如果有多个角色，并且它们是以逗号分隔的字符串存储在单个字段中：
        // return Arrays.stream(roles.split(","))
        //        .map(String::trim)
        //        .map(SimpleGrantedAuthority::new)
        //        .collect(Collectors.toList());

        // 如果是单个角色字符串：
        return Arrays.asList(new SimpleGrantedAuthority(role));
    }
}