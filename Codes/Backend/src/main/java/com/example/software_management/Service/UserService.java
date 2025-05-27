package com.example.software_management.Service;

import java.util.Map;

public interface UserService {
    Map<String, String> getInfo();
    Map<String, String> getToken(String username, String password);
    Map<String, String> register(String username, String password, String confirmedPassword, String email, String avatar);
}
