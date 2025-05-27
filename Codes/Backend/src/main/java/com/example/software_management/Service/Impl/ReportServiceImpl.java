package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Alert;
import com.example.software_management.Repository.AlertRepository;
import com.example.software_management.Service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final AlertRepository alertRepository;

    @Autowired
    public ReportServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public long getAlertCount(Boolean isProcessed, Alert.Severity severity,
                              LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("获取预警数量统计: isProcessed={}, severity={}, startTime={}, endTime={}",
                isProcessed, severity, startTime, endTime);

        try {
            return alertRepository.countAlerts(isProcessed, severity, startTime, endTime);
        } catch (Exception e) {
            logger.error("获取预警数量时发生错误", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getWeeklyAlerts(Boolean isProcessed, Alert.Severity severity, LocalDate weekStartDate) {
        logger.debug("获取周预警统计: isProcessed={}, severity={}, weekStartDate={}",
                isProcessed, severity, weekStartDate);

        try {
            // 确保weekStartDate是周一
            LocalDate monday = weekStartDate;
            if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
                logger.info("提供的日期不是周一，自动调整到前一个周一: {} -> {}",
                        weekStartDate, weekStartDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)));
                monday = weekStartDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
            }

            // 计算一周的结束日期（周日）
            LocalDate sunday = monday.plusDays(6);

            logger.debug("查询日期范围: {} 至 {}", monday, sunday);

            // 查询一周内每天的预警数量
            List<Object[]> dailyCounts = alertRepository.countDailyAlerts(
                    monday.atStartOfDay(), sunday.atTime(23, 59, 59),
                    isProcessed, severity);

            // 将数据库结果转换为所需的返回格式
            Map<LocalDate, Long> countsMap = new HashMap<>();

            // 将查询结果放入Map
            for (Object[] result : dailyCounts) {
                LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
                Long count = ((Number) result[1]).longValue();
                countsMap.put(date, count);
                logger.trace("日期 {} 的预警数量: {}", date, count);
            }

            // 创建最终的返回列表，确保所有7天都有数据
            List<Map<String, Object>> weeklyData = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = monday.plusDays(i);
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("dayOfWeek", dayOfWeek.getValue());
                dayData.put("dayName", dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                dayData.put("count", countsMap.getOrDefault(currentDate, 0L));

                weeklyData.add(dayData);
            }

            return weeklyData;
        } catch (Exception e) {
            logger.error("获取周预警统计时发生错误", e);
            throw e;
        }
    }
}