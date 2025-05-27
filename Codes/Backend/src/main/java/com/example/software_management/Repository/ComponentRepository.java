package com.example.software_management.Repository;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer> {

    /**
     * 检查指定用户下是否存在同名组件
     * @param name 组件名称
     * @param user 用户
     * @return 存在返回true，否则返回false
     */
    boolean existsByNameAndUser(String name, User user);

    /**
     * 查询用户的所有组件
     * @param user 用户
     * @return 组件列表
     */
    List<Component> findByUser(User user);

    /**
     * 查询用户的所有组件（不包含图片和敏感数据）
     * @param user 用户
     * @return 组件信息列表
     */
    @Query("SELECT c.id as id, c.name as name, c.location as location, c.updatedTime as updated_time, " +
            "m.name as model__name, m.id as model__id, c.lifeForecast as life_forecast, c.status as status " +
            "FROM Component c LEFT JOIN c.model m WHERE c.user = :user")
    List<Map<String, Object>> findByUserWithoutSensitiveData(@Param("user") User user);

    /**
     * 查询组件并附带用户信息
     * @param id 组件ID
     * @return 组件对象（可选）
     */
    @Query("SELECT c FROM Component c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Component> findByIdWithUser(@Param("id") Integer id);

    /**
     * 统计每个模型下的组件数量
     * @param user 用户
     * @return 模型名称和组件数量的映射
     */
    @Query("SELECT c.model.name as name, COUNT(c) as count FROM Component c WHERE c.user = :user AND c.model IS NOT NULL GROUP BY c.model.name")
    List<Map<String, Object>> countComponentsByModel(@Param("user") User user);

    /**
     * 统计每个位置的组件数量
     * @param user 用户
     * @return 位置和组件数量的映射
     */
    @Query("SELECT c.location as location, COUNT(c) as count FROM Component c WHERE c.user = :user GROUP BY c.location")
    List<Map<String, Object>> countComponentsByLocation(@Param("user") User user);
}