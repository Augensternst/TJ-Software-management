package com.example.software_management.Service;

import java.util.Map;

public interface UserService {
    Map<String, String> getInfo();
    Map<String, String> getToken(String username, String password) throws Exception;
    Map<String, String> register(String username, String password, String phone) throws Exception;
}