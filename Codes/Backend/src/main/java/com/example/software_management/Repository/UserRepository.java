package com.example.software_management.Repository;

import com.example.software_management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果用户名已存在则为true，否则为false
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名和密码查找用户(用于认证)
     * @param username 用户名
     * @param hashedPassword 哈希密码
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByUsernameAndHashedPassword(String username, String hashedPassword);

    /**
     * 通过电子邮件查找用户
     * @param email 电子邮件
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByEmail(String email);

    /**
     * 通过电话号码查找用户
     * @param phone 电话号码
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByPhone(String phone);

    /**
     * 查询用户头像
     * @param username 用户名
     * @return 可能包含头像二进制数据的Optional对象
     */
    @Query("SELECT u.avatar FROM User u WHERE u.username = :username")
    Optional<byte[]> findAvatarByUsername(@Param("username") String username);
}
