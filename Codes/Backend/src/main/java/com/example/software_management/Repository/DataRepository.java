package com.example.software_management.Repository;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.DData;
import com.example.software_management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataRepository extends JpaRepository<DData, Integer> {

    /**
     * 根据组件ID查找数据
     * @param componentId 组件ID
     * @return 数据列表
     */
    List<DData> findByComponentId(Integer componentId);

    /**
     * 根据用户查询所有数据
     * @param user 用户
     * @return 数据列表
     */
    @Query("SELECT d FROM DData d JOIN d.component c WHERE c.user = :user")
    List<DData> findByUser(@Param("user") User user);

    /**
     * 查询用户的所有数据（不包含文件内容）
     * @param user 用户
     * @return 数据列表（Map形式，包含id、name、time、result、component_id和component_name）
     */
    @Query("SELECT d.id as id, d.name as name, d.time as time, d.result as result, " +
            "c.id as component_id, c.name as component_name " +
            "FROM DData d JOIN d.component c WHERE c.user = :user")
    List<Map<String, Object>> findByUserWithoutFile(@Param("user") User user);

    /**
     * 统计用户的数据总数
     * @param user 用户
     * @return 数据总数
     */
    @Query("SELECT COUNT(d) FROM DData d JOIN d.component c WHERE c.user = :user")
    Long countByUser(@Param("user") User user);
}