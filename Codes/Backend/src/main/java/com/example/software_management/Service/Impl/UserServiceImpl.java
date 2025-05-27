package com.example.software_management.Service.Impl;

import com.example.software_management.Service.UserService;
import com.example.software_management.Repository.UserRepository;
import com.example.software_management.Model.User;
import com.example.software_management.Jwt.JwtUtil;
import com.example.software_management.Security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public Map<String, String> getInfo() {
        // 获取当前用户的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> info = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                info.put("username", user.getUsername());
                info.put("email", user.getEmail());
                info.put("phone", user.getPhone());
                
                if (user.getRole() != null) {
                    info.put("role", String.valueOf(user.getRole()));
                } else {
                    info.put("role", "0"); // 默认角色
                }
                
                // 如果存在头像数据，转换为Base64字符串
                if (user.getAvatar() != null && user.getAvatar().length > 0) {
                    String avatarBase64 = Base64.getEncoder().encodeToString(user.getAvatar());
                    info.put("avatar", avatarBase64);
                }
            } else {
                info.put("error", "用户不存在");
            }
        } else {
            info.put("error", "未认证用户");
        }
        
        return info;
    }

    @Override
    public Map<String, String> getToken(String username, String password) {
        // 实现获取用户令牌的逻辑
        Map<String, String> response = new HashMap<>();
        
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 验证密码
            if (passwordEncoder.matches(password, user.getHashedPassword())) {
                // 创建用户安全对象
                UserSecurity userSecurity = new UserSecurity(user);
                
                // 生成JWT令牌
                String jwt = JwtUtil.createJWT(username);
                response.put("token", jwt);
                response.put("status", "success");
            } else {
                response.put("status", "error");
                response.put("message", "密码错误");
            }
        } else {
            response.put("status", "error");
            response.put("message", "用户不存在");
        }
        
        return response;
    }

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword, String email, String avatar) {
        // 实现用户注册的逻辑
        Map<String, String> response = new HashMap<>();
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            response.put("status", "error");
            response.put("message", "用户名已存在");
            return response;
        }
        
        // 检查两次输入的密码是否一致
        if (!password.equals(confirmedPassword)) {
            response.put("status", "error");
            response.put("message", "两次输入的密码不一致");
            return response;
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setHashedPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setPhone(""); // 默认为空，可以后续更新
        newUser.setRole(0);   // 默认普通用户角色
        
        // 保存用户
        userRepository.save(newUser);
        
        response.put("status", "success");
        response.put("message", "注册成功");
        return response;
    }
}
