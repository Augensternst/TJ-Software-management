package com.example.software_management.Jwt;

import com.example.software_management.Model.User;
import com.example.software_management.Repository.UserRepository;
import com.example.software_management.Security.UserSecurity;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * JWT认证过滤器
 * 拦截请求，从Authorization头中提取JWT令牌，验证并设置用户认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 从请求头中获取JWT令牌
        String token = extractJwtFromRequest(request);

        // 如果请求中没有有效的JWT令牌，直接放行请求
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 解析JWT令牌
            Claims claims = JwtUtil.parseJWT(token);
            String username = claims.getSubject();

            // 根据用户名从数据库中查询用户
            Optional<User> userOptional = userRepository.findByUsername(username);

            // 如果用户存在，则设置认证信息
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                setAuthenticationContext(user);
            }
        } catch (Exception e) {
            // 令牌解析失败时，记录错误但不抛出异常，继续处理请求
            logger.error("无法验证JWT令牌", e);
            // 可以考虑在此处添加日志记录或监控
        }

        // 继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 设置Spring Security认证上下文
     * @param user 用户对象
     */
    private void setAuthenticationContext(User user) {
        // 创建UserSecurity对象
        UserSecurity userSecurity = new UserSecurity(user);

        // 创建认证令牌
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userSecurity,                 // principal (当前用户)
                        null,                         // credentials (凭证，已验证所以为null)
                        userSecurity.getAuthorities() // 用户权限
                );

        // 设置认证上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}