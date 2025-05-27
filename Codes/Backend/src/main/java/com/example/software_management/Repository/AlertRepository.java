package com.example.software_management.Repository;

import com.example.software_management.Model.Alert;
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
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * 根据多条件查询预警
     * @param componentId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param severity 严重性
     * @param isProcessed 是否已处理
     * @param pageable 分页参数
     * @return 预警分页结果
     */
    @Query("SELECT a FROM Alert a WHERE (:componentId IS NULL OR a.component.id = :componentId) " +
            "AND (:startTime IS NULL OR a.alertTime >= :startTime) " +
            "AND (:endTime IS NULL OR a.alertTime <= :endTime) " +
            "AND (:severity IS NULL OR a.severity = :severity) " +
            "AND (:isProcessed IS NULL OR a.isProcessed = :isProcessed) " +
            "ORDER BY a.alertTime DESC")
    Page<Alert> findAlerts(
            @Param("componentId") Integer componentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("severity") Alert.Severity severity,
            @Param("isProcessed") Boolean isProcessed,
            Pageable pageable);

    /**
     * 批量更新预警为已处理状态
     * @param alertIds 预警ID列表
     * @param processedBy 处理人
     * @param processedAt 处理时间
     * @return 影响的行数
     */
    @Modifying
    @Query("UPDATE Alert a SET a.isProcessed = true, a.processedBy.username = :processedBy, " +
            "a.processedAt = :processedAt WHERE a.id IN :alertIds")
    int batchProcessAlerts(
            @Param("alertIds") List<Long> alertIds,
            @Param("processedBy") String processedBy,
            @Param("processedAt") LocalDateTime processedAt);

    /**
     * 统计特定条件下的预警数量
     * @param isProcessed 是否已处理
     * @param severity 严重性
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 符合条件的预警数量
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE (:isProcessed IS NULL OR a.isProcessed = :isProcessed) " +
            "AND (:severity IS NULL OR a.severity = :severity) " +
            "AND (:startTime IS NULL OR a.alertTime >= :startTime) " +
            "AND (:endTime IS NULL OR a.alertTime <= :endTime)")
    long countAlerts(
            @Param("isProcessed") Boolean isProcessed,
            @Param("severity") Alert.Severity severity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定日期范围内每天的预警数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isProcessed 是否已处理
     * @param severity 严重性
     * @return 每天的预警数量
     */
    @Query("SELECT FUNCTION('DATE', a.alertTime) as date, COUNT(a) as count FROM Alert a " +
            "WHERE a.alertTime BETWEEN :startDate AND :endDate " +
            "AND (:isProcessed IS NULL OR a.isProcessed = :isProcessed) " +
            "AND (:severity IS NULL OR a.severity = :severity) " +
            "GROUP BY FUNCTION('DATE', a.alertTime) " +
            "ORDER BY FUNCTION('DATE', a.alertTime)")  // 改为按日期排序
    List<Object[]> countDailyAlerts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("isProcessed") Boolean isProcessed,
            @Param("severity") Alert.Severity severity);
}