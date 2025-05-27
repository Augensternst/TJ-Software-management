package com.example.software_management.Controller;

import com.example.software_management.Exception.ResourceNotFoundException;
import com.example.software_management.Model.Alert;
import com.example.software_management.Model.User;
import com.example.software_management.Security.UserSecurity;
import com.example.software_management.Service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * 获取预警列表
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> getAlerts(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer componentId = requestBody.get("componentId") != null ?
                    Integer.valueOf(requestBody.get("componentId").toString()) : null;
            String startTimeStr = (String) requestBody.get("startTime");
            String endTimeStr = (String) requestBody.get("endTime");
            String severityStr = (String) requestBody.get("severity");
            Boolean isProcessed = (Boolean) requestBody.get("isProcessed");

            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
            Alert.Severity severity = severityStr != null ? Alert.Severity.valueOf(severityStr) : null;

            // 获取分页参数
            Map<String, Object> pagination = (Map<String, Object>) requestBody.get("pagination");
            int page = pagination != null ? ((Number) pagination.get("page")).intValue() : 0;
            int size = pagination != null ? ((Number) pagination.get("size")).intValue() : 20;

            // 查询预警
            Page<Alert> alerts = alertService.getAlerts(componentId, startTime, endTime, severity, isProcessed, page, size);

            // 转换为API响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, Object> data = new HashMap<>();
            data.put("content", convertAlertsToResponse(alerts.getContent()));
            data.put("totalElements", alerts.getTotalElements());
            data.put("totalPages", alerts.getTotalPages());

            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            logger.error("日期时间格式错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_DATE_FORMAT", "日期时间格式错误: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("参数错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMETER", "参数错误: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("获取预警列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 批量处理预警
     */
    @PostMapping("/batch-process")
    public ResponseEntity<Map<String, Object>> batchProcessAlerts(@RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("alertIds") ) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MISSING_REQUIRED_FIELDS", "缺少必要字段: alertIds 或 processedBy"));
            }

            List<Integer> alertIds = (List<Integer>) requestBody.get("alertIds");


            if (alertIds == null || alertIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("EMPTY_ALERT_IDS", "预警ID列表不能为空"));
            }



            // 转换Integer列表为Long列表
            List<Long> longAlertIds = alertIds.stream()
                    .map(Long::valueOf)
                    .toList();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();
            String processedBy= currentUser.getUsername();

            int affectedRows = alertService.batchProcessAlerts(longAlertIds, processedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("affectedRows", affectedRows);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("参数错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMETER", "参数错误: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("批量处理预警时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 批量删除预警
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, Object>> batchDeleteAlerts(@RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("alertIds")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MISSING_REQUIRED_FIELDS", "缺少必要字段: alertIds"));
            }

            List<Integer> alertIds = (List<Integer>) requestBody.get("alertIds");

            if (alertIds == null || alertIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("EMPTY_ALERT_IDS", "预警ID列表不能为空"));
            }

            // 转换Integer列表为Long列表
            List<Long> longAlertIds = alertIds.stream()
                    .map(Long::valueOf)
                    .toList();

            int affectedRows = alertService.batchDeleteAlerts(longAlertIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("affectedRows", affectedRows);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量删除预警时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 创建预警
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createAlert(@RequestBody Map<String, Object> requestBody) {
        try {
            // 验证必要字段
            if (!requestBody.containsKey("componentId") || !requestBody.containsKey("severity") ||
                    !requestBody.containsKey("details") || !requestBody.containsKey("alertTime")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MISSING_REQUIRED_FIELDS", "缺少必要字段"));
            }

            Integer componentId = Integer.valueOf(requestBody.get("componentId").toString());
            String severityStr = (String) requestBody.get("severity");
            String details = (String) requestBody.get("details");
            String alertTimeStr = (String) requestBody.get("alertTime");

            if (details == null || details.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("INVALID_DETAILS", "预警详情不能为空"));
            }

            Alert.Severity severity = Alert.Severity.valueOf(severityStr);
            LocalDateTime alertTime = LocalDateTime.parse(alertTimeStr);

            Alert createdAlert = alertService.createAlert(componentId, severity, details, alertTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, Object> data = new HashMap<>();
            data.put("id", createdAlert.getId());
            data.put("alertTime", createdAlert.getAlertTime().toString());
            data.put("severity", createdAlert.getSeverity().toString());
            data.put("details", createdAlert.getDetails());

            response.put("data", data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DateTimeParseException e) {
            logger.error("日期时间格式错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_DATE_FORMAT", "日期时间格式错误: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("参数错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMETER", "参数错误: " + e.getMessage()));
        } catch (ResourceNotFoundException e) {
            logger.error("资源未找到", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            logger.error("创建预警时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 导出预警报表
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportAlerts(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer componentId = requestBody.get("componentId") != null ?
                    Integer.valueOf(requestBody.get("componentId").toString()) : null;
            String startTimeStr = (String) requestBody.get("startTime");
            String endTimeStr = (String) requestBody.get("endTime");
            String severityStr = (String) requestBody.get("severity");
            Boolean isProcessed = (Boolean) requestBody.get("isProcessed");
            Integer limit = requestBody.get("limit") != null ?
                    Integer.valueOf(requestBody.get("limit").toString()) : null;

            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
            Alert.Severity severity = severityStr != null ? Alert.Severity.valueOf(severityStr) : null;

            byte[] reportData = alertService.exportAlerts(componentId, startTime, endTime, severity, isProcessed, limit);

            if (reportData == null || reportData.length == 0) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(createErrorResponse("NO_DATA", "没有符合条件的数据可导出"));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "alerts_report.xlsx");
            headers.setContentLength(reportData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);
        } catch (DateTimeParseException e) {
            logger.error("日期时间格式错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_DATE_FORMAT", "日期时间格式错误: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("参数错误", e);
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PARAMETER", "参数错误: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("导出预警报表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 辅助方法：将Alert实体转换为API响应格式
     */
    private List<Map<String, Object>> convertAlertsToResponse(List<Alert> alerts) {
        return alerts.stream().map(alert -> {
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("id", alert.getId());
            alertMap.put("componentName", alert.getComponent().getName());
            alertMap.put("alertTime", alert.getAlertTime().toString());
            alertMap.put("severity", alert.getSeverity().toString());
            alertMap.put("details", alert.getDetails());
            alertMap.put("isProcessed", alert.getIsProcessed());
            return alertMap;
        }).toList();
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
