package com.example.software_management.Controller;

import com.example.software_management.DTO.AlertDTO;
import com.example.software_management.Service.AlertService;
import com.example.software_management.Security.GetInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * 4.1 获取用户未确认的警报设备
     * @param requestBody 包含查询参数的请求体
     * @return 未确认的警报分页结果
     */
    @PostMapping("/getUnconfirmedAlerts")
    public ResponseEntity<Map<String, Object>> getUnconfirmedAlerts(
            @RequestBody Map<String, Object> requestBody) {

        int userId = GetInfo.getCurrentUserId();

        // 从请求体中获取查询参数
        String deviceName = (String) requestBody.getOrDefault("deviceName", null);
        String startTime = (String) requestBody.getOrDefault("startTime", null);
        String endTime = (String) requestBody.getOrDefault("endTime", null);

        // 获取分页参数，如果不存在使用默认值
        int page = requestBody.containsKey("page") ?
                Integer.parseInt(requestBody.get("page").toString()) : 1;
        int pageSize = requestBody.containsKey("pageSize") ?
                Integer.parseInt(requestBody.get("pageSize").toString()) : 10;

        Page<AlertDTO> alertPage = alertService.getUnconfirmedAlerts(
                userId, deviceName, startTime, endTime, page, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", alertPage.getTotalElements());
        response.put("alerts", alertPage.getContent());

        return ResponseEntity.ok(response);
    }
    /**
     * 4.2 批量更新设备警报状态为已确认
     * @param requestBody 包含警报ID列表的请求体
     * @return 确认结果
     */
    @PutMapping("/confirmAlert")
    public ResponseEntity<Map<String, Object>> confirmAlerts(
            @RequestBody Map<String, List<Integer>> requestBody) {

        int userId = GetInfo.getCurrentUserId();
        List<Integer> alertIds = requestBody.get("alertIds");
        Map<String, Object> result = alertService.confirmAlerts(alertIds, userId);

        return ResponseEntity.ok(result);
    }

    /**
     * 4.3 批量删除警报
     * @param requestBody 包含警报ID列表的请求体
     * @return 删除结果
     */
    @DeleteMapping("/deleteAlerts")
    public ResponseEntity<Map<String, Object>> deleteAlerts(
            @RequestBody Map<String, List<Integer>> requestBody) {

        List<Integer> alertIds = requestBody.get("alertIds");
        Map<String, Object> result = alertService.deleteAlerts(alertIds);

        return ResponseEntity.ok(result);
    }

    /**
     * 4.4 导出用户未确认的警报设备至XLSX
     * @return XLSX文件
     */
    @GetMapping("/exportAlertsToXLSX")
    public ResponseEntity<Resource> exportAlertsToXLSX() {

        int userId = GetInfo.getCurrentUserId();
        Resource resource = alertService.exportAlertsToXLSX(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=unconfirmed_alerts_" + userId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * 4.5 预警状态分布
     * @return 各状态的警报数量
     */
    @GetMapping("/status-summary")
    public ResponseEntity<Map<String, Object>> getAlertStatusSummary() {

        int userId = GetInfo.getCurrentUserId();
        List<Map<String, Object>> statusSummary = alertService.getAlertStatusSummary(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statusSummary", statusSummary);

        return ResponseEntity.ok(response);
    }
}