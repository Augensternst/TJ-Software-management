package com.example.software_management.Service;

import com.example.software_management.Model.Alert;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {
    /**
     * 获取预警数量统计
     * @param isProcessed 是否已处理
     * @param severity 严重性
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预警数量
     */
    long getAlertCount(Boolean isProcessed, Alert.Severity severity, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取周预警统计
     * @param isProcessed 是否已处理
     * @param severity 严重性
     * @param weekStartDate 周开始日期(周一)
     * @return 每天的预警统计数据
     */
    List<Map<String, Object>> getWeeklyAlerts(Boolean isProcessed, Alert.Severity severity, LocalDate weekStartDate);
}