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

    /**
     * 分页查询用户的所有组件
     * @param userId 用户ID
     * @param searchQuery 搜索关键词（可选）
     * @param pageable 分页参数
     * @return 组件分页结果
     */
    @Query("SELECT c FROM Component c WHERE c.user.id = :userId " +
            "AND (:searchQuery IS NULL OR c.name LIKE %:searchQuery%)")
    Page<Component> findByUserIdWithSearch(
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
     * 查询组件的健康数据趋势（最近7天）
     * @param componentId 组件ID
     * @return 组件健康数据
     */
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.HPT_eff_mod + d.nf + d.sm_fan + d.t24 + d.wf + d.t48 + d.nc + d.sm_hpc) / 8 as health_index " +
                    "FROM device_data d " +
                    "WHERE d.component_id = :componentId " +
                    "AND d.time >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(d.time) " +
                    "ORDER BY DATE(d.time)")
    List<Double> getComponentHealthTrend(@Param("componentId") Integer componentId);

    /**
     * 查询组件的能耗数据趋势（最近7天）
     * @param componentId 组件ID
     * @return 组件能耗数据
     */
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.wf) as energy_consumption " +
                    "FROM device_data d " +
                    "WHERE d.component_id = :componentId " +
                    "AND d.time >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(d.time) " +
                    "ORDER BY DATE(d.time)")
    List<Double> getComponentEnergyTrend(@Param("componentId") Integer componentId);

    /**
     * 查询组件的当前能耗成本
     * @param componentId 组件ID
     * @return 能耗成本
     */
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.wf) * 0.75 as energy_cost " +
                    "FROM device_data d " +
                    "WHERE d.component_id = :componentId " +
                    "AND DATE(d.time) = CURRENT_DATE()")
    Double getComponentEnergyCost(@Param("componentId") Integer componentId);

    /**
     * 查询组件的关键指标卡片数据
     * @param componentId 组件ID
     * @return 指标卡片数据
     */
    @Query("SELECT 'HPT效率' as name, d.hptEffMod as value, '%' as unit, " +
            "CASE WHEN d.hptEffMod < 60 THEN -1 ELSE d.hptEffMod END as health " +
            "FROM Data d WHERE d.component.id = :componentId " +
            "ORDER BY d.time DESC")
    List<Map<String, Object>> getComponentMetricCards(@Param("componentId") Integer componentId, Pageable pageable);

    /**
     * 计算组件关键指标卡片的总页数
     * @param componentId 组件ID
     * @param pageSize 每页数量
     * @return 总页数
     */
    @Query("SELECT CEIL(COUNT(DISTINCT 'HPT效率') / :pageSize) FROM Data d WHERE d.component.id = :componentId")
    int getComponentMetricCardsTotalPages(@Param("componentId") Integer componentId, @Param("pageSize") int pageSize);
}