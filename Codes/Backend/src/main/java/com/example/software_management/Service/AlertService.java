//==============================================
// 预警系统模块 (Alert)
//==============================================

package com.example.software_management.Service;

import com.example.software_management.Model.Alert;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertService {
    /**
     * 获取预警列表
     * @param componentId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param severity 严重性
     * @param isProcessed 是否已处理
     * @param page 页码
     * @param size 每页大小
     * @return 分页后的预警列表
     */
    Page<Alert> getAlerts(Integer componentId, LocalDateTime startTime, LocalDateTime endTime,
                          Alert.Severity severity, Boolean isProcessed, int page, int size);

    /**
     * 批量处理预警
     * @param alertIds 预警ID列表
     * @param processedBy 处理人用户名
     * @return 成功处理的预警数量
     */
    int batchProcessAlerts(List<Long> alertIds, String processedBy);

    /**
     * 批量删除预警
     * @param alertIds 预警ID列表
     * @return 成功删除的预警数量
     */
    int batchDeleteAlerts(List<Long> alertIds);

    /**
     * 创建预警
     * @param componentId 设备ID
     * @param severity 严重性
     * @param details 详情
     * @param alertTime 预警时间
     * @return 创建的预警
     */
    Alert createAlert(Integer componentId, Alert.Severity severity, String details, LocalDateTime alertTime);

    /**
     * 导出预警报表
     * @param componentId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param severity 严重性
     * @param isProcessed 是否已处理
     * @param limit 限制数量
     * @return 二进制文件数据
     */
    byte[] exportAlerts(Integer componentId, LocalDateTime startTime, LocalDateTime endTime,
                        Alert.Severity severity, Boolean isProcessed, Integer limit);
}