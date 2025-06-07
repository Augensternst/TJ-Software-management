package com.example.software_management.Repository;

import com.example.software_management.Model.Alert;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {

    // 删除警报
    @Modifying
    @Transactional
    @Query("DELETE FROM Alert a WHERE a.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Integer> ids);

    /**
     * 查询用户未确认的警报设备
     * @param userId 用户ID
     * @param deviceName 设备名称（可选，模糊匹配）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 警报分页结果
     */
    @Query("SELECT a FROM Alert a JOIN a.component c WHERE c.user.id = :userId " +
            "AND a.isConfirmed = false " +
            "AND (:deviceName IS NULL OR c.name LIKE CONCAT('%', :deviceName, '%')) " +
            "AND (:startTime IS NULL OR a.alertTime >= :startTime) " +
            "AND (:endTime IS NULL OR a.alertTime <= :endTime) " +
            "ORDER BY a.alertTime DESC")
    Page<Alert> findUnconfirmedAlerts(
            @Param("userId") Integer userId,
            @Param("deviceName") String deviceName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 批量更新警报为已确认状态
     * @param alertIds 警报ID列表
     * @param confirmedBy 确认人
     * @param confirmedTime 确认时间
     * @return 影响的行数
     */
    @Modifying
    @Query("UPDATE Alert a SET a.isConfirmed = true, a.confirmedBy.id = :confirmedBy, " +
            "a.confirmedTime = :confirmedTime WHERE a.id IN :alertIds")
    int confirmAlerts(
            @Param("alertIds") List<Integer> alertIds,
            @Param("confirmedBy") Integer confirmedBy,
            @Param("confirmedTime") LocalDateTime confirmedTime);

    /**
     * 获取用户所有警报的状态分布
     * @param userId 用户ID
     * @return 各状态的警报数量
     */
    @Query("SELECT a.status as status, COUNT(a) as count FROM Alert a " +
            "JOIN a.component c WHERE c.user.id = :userId " +
            "GROUP BY a.status")
    List<Object[]> getAlertStatusSummary(@Param("userId") Integer userId);

    /**
     * 获取指定时间范围内的警报统计
     * @param userId 用户ID
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 警报统计信息
     */
    @Query("SELECT a.isConfirmed as isConfirmed, COUNT(a) as count " +
            "FROM Alert a " +
            "JOIN a.component c " +
            "WHERE c.user.id = :userId " +
            "AND a.alertTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY a.isConfirmed")
    List<Object[]> getAlertStatsByTimeRange(
            @Param("userId") Integer userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 获取所有警报统计
     * @param userId 用户ID
     * @return 警报统计信息
     */
    @Query("SELECT a.isConfirmed as isConfirmed, COUNT(a) as count " +
            "FROM Alert a " +
            "JOIN a.component c " +
            "WHERE c.user.id = :userId " +
            "GROUP BY a.isConfirmed")
    List<Object[]> getAllAlertStats(@Param("userId") Integer userId);

    /**
     * 获取每日警报统计
     * @param userId 用户ID
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 每日警报统计信息
     */
    @Query(value = "SELECT DATE(a.alert_time) as date, a.is_confirmed as isConfirmed, COUNT(*) as count " +
            "FROM alert a " +
            "JOIN component c ON a.component_id = c.id " +
            "WHERE c.user_id = :userId " +
            "AND a.alert_time BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY DATE(a.alert_time), a.is_confirmed " +
            "ORDER BY DATE(a.alert_time)", nativeQuery = true)
    List<Object[]> getDailyAlertStats(
            @Param("userId") Integer userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}