package com.example.software_management.Security;

import com.example.software_management.Model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class GetInfo {
    static public int getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
        User currentUser = userSecurity.getUser();
        return currentUser.getId();
    }
}
