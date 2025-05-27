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
public interface AlertRepository extends JpaRepository<Alert, Integer> {

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
            "AND (:deviceName IS NULL OR c.name LIKE %:deviceName%) " +
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
     * 根据组件ID查找警报
     * @param componentId 组件ID
     * @return 警报列表
     */
    List<Alert> findByComponentId(Integer componentId);

    /**
     * 根据组件ID和确认状态查找警报
     * @param componentId 组件ID
     * @param isConfirmed 是否已确认
     * @return 警报列表
     */
    List<Alert> findByComponentIdAndIsConfirmed(Integer componentId, Boolean isConfirmed);

    /**
     * 统计用户未确认的警报数量
     * @param userId 用户ID
     * @return 未确认的警报数量
     */
    @Query("SELECT COUNT(a) FROM Alert a JOIN a.component c WHERE c.user.id = :userId AND a.isConfirmed = false")
    long countUnconfirmedAlertsByUserId(@Param("userId") Integer userId);
}