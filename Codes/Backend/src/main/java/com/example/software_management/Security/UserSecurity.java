package com.example.software_management.Security;

import com.example.software_management.Model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserSecurity implements UserDetails {

    @Getter
    private final User user;

    private final Collection<GrantedAuthority> authorities;

    public UserSecurity(User user) {
        this.user = user;
        this.authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 获取用户ID，适用于JWT subject
     */
    public String getUserIdentifier() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 创建JWT所需的用户信息映射
     */
    public UserJwtInfo toJwtInfo() {
        return new UserJwtInfo(
                this.getUserIdentifier(),
                this.getUsername(),
                this.user.getPhone(),
                getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }

    /**
     * JWT相关用户信息的内部类
     */
    @Getter
    public static class UserJwtInfo {
        private final String username;
        private final String usernameDisplay;
        private final String phone;
        private final List<String> authorities;

        public UserJwtInfo(String username, String usernameDisplay, String phone, List<String> authorities) {
            this.username = username;
            this.usernameDisplay = usernameDisplay;
            this.phone = phone;
            this.authorities = authorities;
        }
    }
}