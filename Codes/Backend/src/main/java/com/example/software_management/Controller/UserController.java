package com.example.software_management.Controller;
import com.example.software_management.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) { // 修正：将参数类型从UserController改为UserService
        this.userService = userService;
    }

    @GetMapping("/user/account/info/")
    public Map<String, String> getInfo() {
        return userService.getInfo();
    }

    @PostMapping("/user/account/token/")
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userService.getToken(username, password);
    }

    @PostMapping("/user/account/register/")
    public Map<String, String> register(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");
        String email = map.get("email");
        String avatar = map.get("avatar");
        return userService.register(username, password, confirmedPassword, email, avatar);
    }
}


