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
                info.put("phone", user.getPhone());
            } else {
                info.put("error", "用户不存在");
            }
        } else {
            info.put("error", "未认证用户");
        }

        return info;
    }

    @Override
    public Map<String, String> getToken(String username, String password) throws Exception {
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
                // 密码错误，抛出异常
                throw new Exception("密码错误");
            }
        } else {
            // 用户不存在，抛出异常
            throw new Exception("用户不存在");
        }

        return response;
    }

    @Override
    public Map<String, String> register(String username, String password, String phone) throws Exception {
        // 实现用户注册的逻辑
        Map<String, String> response = new HashMap<>();

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new Exception("用户名已存在");
        }

        // 检查手机号是否已存在
        if (userRepository.existsByPhone(phone)) {
            throw new Exception("手机号已被注册");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setHashedPassword(passwordEncoder.encode(password));
        newUser.setPhone(phone);

        // 保存用户
        userRepository.save(newUser);

        response.put("status", "success");
        response.put("message", "注册成功");
        return response;
    }
}