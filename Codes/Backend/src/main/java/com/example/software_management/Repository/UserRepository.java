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
     * 检查电话号码是否已存在
     * @param phone 电话号码
     * @return 如果电话号码已存在则为true，否则为false
     */
    boolean existsByPhone(String phone);

}