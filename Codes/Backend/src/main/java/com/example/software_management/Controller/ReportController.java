//==============================================
// 报表生成模块 - Controller
//==============================================

package com.example.software_management.Controller;

import com.example.software_management.Model.Alert;
import com.example.software_management.Service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 预警数量统计
     */
    @PostMapping("/alert-count")
    public ResponseEntity<Map<String, Object>> getAlertCount(@RequestBody Map<String, Object> requestBody) {
        logger.debug("接收到预警数量统计请求: {}", requestBody);

        try {
            // 参数验证和转换
            Boolean isProcessed = (Boolean) requestBody.get("isProcessed");
            String severityStr = (String) requestBody.get("severity");
            String startTimeStr = (String) requestBody.get("startTime");
            String endTimeStr = (String) requestBody.get("endTime");

            Alert.Severity severity = null;
            if (severityStr != null && !severityStr.isEmpty()) {
                try {
                    severity = Alert.Severity.valueOf(severityStr);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的严重性级别: {}", severityStr);
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("INVALID_SEVERITY", "无效的严重性级别: " + severityStr));
                }
            }

            LocalDateTime startTime = null;
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                try {
                    startTime = LocalDateTime.parse(startTimeStr);
                } catch (DateTimeParseException e) {
                    logger.warn("无效的开始时间格式: {}", startTimeStr);
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("INVALID_START_TIME", "无效的开始时间格式: " + startTimeStr));
                }
            }

            LocalDateTime endTime = null;
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                try {
                    endTime = LocalDateTime.parse(endTimeStr);
                } catch (DateTimeParseException e) {
                    logger.warn("无效的结束时间格式: {}", endTimeStr);
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("INVALID_END_TIME", "无效的结束时间格式: " + endTimeStr));
                }
            }

            // 检查时间范围
            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                logger.warn("开始时间晚于结束时间: {} > {}", startTimeStr, endTimeStr);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("INVALID_TIME_RANGE", "开始时间不能晚于结束时间"));
            }

            // 调用服务获取结果
            long count = reportService.getAlertCount(isProcessed, severity, startTime, endTime);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取预警数量统计时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 周预警统计
     */
    @PostMapping("/weekly-alerts")
    public ResponseEntity<Map<String, Object>> getWeeklyAlerts(@RequestBody Map<String, Object> requestBody) {
        logger.debug("接收到周预警统计请求: {}", requestBody);

        try {
            // 参数验证和转换
            Boolean isProcessed = (Boolean) requestBody.get("isProcessed");
            String severityStr = (String) requestBody.get("severity");
            String weekStartDateStr = (String) requestBody.get("weekStartDate");

            // 验证必要参数
            if (weekStartDateStr == null || weekStartDateStr.isEmpty()) {
                logger.warn("缺少必要参数: weekStartDate");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MISSING_REQUIRED_PARAM", "缺少必要参数: weekStartDate"));
            }

            Alert.Severity severity = null;
            if (severityStr != null && !severityStr.isEmpty()) {
                try {
                    severity = Alert.Severity.valueOf(severityStr);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的严重性级别: {}", severityStr);
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("INVALID_SEVERITY", "无效的严重性级别: " + severityStr));
                }
            }

            LocalDate weekStartDate;
            try {
                weekStartDate = LocalDate.parse(weekStartDateStr);
            } catch (DateTimeParseException e) {
                logger.warn("无效的日期格式: {}", weekStartDateStr);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("INVALID_DATE_FORMAT", "无效的日期格式: " + weekStartDateStr));
            }

            // 调用服务获取结果
            List<Map<String, Object>> weeklyData = reportService.getWeeklyAlerts(isProcessed, severity, weekStartDate);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", weeklyData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取周预警统计时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 创建统一的错误响应
     */
    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}