package com.example.software_management.Repository;

import com.example.software_management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

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
     * 通过电话号码查找用户
     * @param phone 电话号码
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查电话号码是否已存在
     * @param phone 电话号码
     * @return 如果电话号码已存在则为true，否则为false
     */
    boolean existsByPhone(String phone);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 可能包含用户的Optional对象
     */
    Optional<User> findByEmail(String email);

    /**
     * 获取用户拥有的设备数量
     * @param userId 用户ID
     * @return 设备数量
     */
    @Query("SELECT COUNT(c) FROM Component c WHERE c.user.id = :userId")
    long countUserDevices(@Param("userId") Integer userId);

    /**
     * 获取用户拥有的有缺陷的设备数量（状态 ≠ 1）
     * @param userId 用户ID
     * @return 有缺陷的设备数量
     */
    @Query("SELECT COUNT(c) FROM Component c WHERE c.user.id = :userId AND c.status != 1")
    long countDefectiveDevices(@Param("userId") Integer userId);

    /**
     * 获取用户未确认的警报数量
     * @param userId 用户ID
     * @return 未确认的警报数量
     */
    @Query("SELECT COUNT(a) FROM Alert a JOIN a.component c WHERE c.user.id = :userId AND a.isConfirmed = false")
    long countUnconfirmedAlerts(@Param("userId") Integer userId);
}