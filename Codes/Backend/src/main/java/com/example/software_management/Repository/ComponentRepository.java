package com.example.software_management.Repository;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer> {

    /**
     * 查询用户的所有组件数量
     * @param userId 用户ID
     * @return 组件数量
     */
    long countByUserId(Integer userId);

    @Query("SELECT c FROM Component c WHERE c.user.id = :userId")
    Page<Component> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT c FROM Component c WHERE c.user.id = :userId AND c.name LIKE %:searchQuery%")
    Page<Component> findByUserIdAndNameContaining(
            @Param("userId") Integer userId,
            @Param("searchQuery") String searchQuery,
            Pageable pageable);

    /**
     * 查询用户所有组件的状态分布
     * @param userId 用户ID
     * @return 各状态的组件数量
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM Component c " +
            "WHERE c.user.id = :userId GROUP BY c.status")
    List<Object[]> getComponentStatusSummary(@Param("userId") Integer userId);

    /**
     * 查询用户所有有缺陷的组件（状态 ≠ 1）
     * @param userId 用户ID
     * @return 有缺陷的组件列表
     */
    @Query("SELECT c FROM Component c WHERE c.user.id = :userId AND c.status != 1")
    List<Component> findDefectiveComponents(@Param("userId") Integer userId);


    /**
     * 查询组件的健康指数趋势（最近7天），包含日期
     * @param componentId 组件ID
     * @return 日期和健康指数趋势数据
     */
    @Query(nativeQuery = true,
            value = "SELECT DATE(f.forecast_time) as date, AVG(f.health_index) as health_index " +
                    "FROM forecast f " +
                    "WHERE f.component_id = :componentId " +
                    "AND f.forecast_time >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(f.forecast_time) " +
                    "ORDER BY DATE(f.forecast_time)")
    List<Object[]> getComponentHealthTrendWithDates(@Param("componentId") Integer componentId);

    /**
     * 查询组件的能耗数据趋势（最近7天），包含日期
     * @param componentId 组件ID
     * @return 日期和能耗数据趋势
     */
    @Query(nativeQuery = true,
            value = "SELECT DATE(d.time) as date, AVG(d.wf) as energy_consumption " +
                    "FROM device_data d " +
                    "WHERE d.component_id = :componentId " +
                    "AND d.time >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(d.time) " +
                    "ORDER BY DATE(d.time)")
    List<Object[]> getComponentEnergyTrendWithDates(@Param("componentId") Integer componentId);



    /**
     * 查询组件的当前能耗成本
     * @param componentId 组件ID
     * @return 能耗成本
     */
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.wf) as energy_cost " +
                    "FROM device_data d " +
                    "WHERE d.component_id = :componentId " +
                    "AND DATE(d.time) = CURRENT_DATE()")
    Double getComponentEnergyCost(@Param("componentId") Integer componentId);


}