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
        this.authorities = mapRolesToAuthorities(user.getRole());
    }

    /**
     * 将用户角色转换为Spring Security的权限
     */
    private Collection<GrantedAuthority> mapRolesToAuthorities(Integer roleCode) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 基本用户角色
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 根据角色代码添加不同级别的权限
        switch (roleCode) {
            case 1:
                authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                break;
            case 2:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                break;
            case 3:
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                break;
            default:
                // 默认只有基本用户权限
                break;
        }

        return authorities;
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

    /**
     * 获取用户邮箱，可用于JWT claims
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 获取用户角色，可用于JWT claims
     */
    public Integer getRole() {
        return user.getRole();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 在此系统中账户不会过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 可以基于某些标志位实现锁定功能
        // 目前假设所有账户都未锁定
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 可以实现密码过期逻辑
        // 目前假设所有凭证都未过期
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 可以基于用户状态实现禁用功能
        // 目前假设所有用户都已启用
        return true;
    }



    /**
     * 创建JWT所需的用户信息映射
     */
    public UserJwtInfo toJwtInfo() {
        return new UserJwtInfo(
                this.getUserIdentifier(),
                this.getEmail(),
                this.getRole(),
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
        private final String email;
        private final Integer role;
        private final List<String> authorities;

        public UserJwtInfo(String username, String email, Integer role, List<String> authorities) {
            this.username = username;
            this.email = email;
            this.role = role;
            this.authorities = authorities;
        }
    }
}
