package com.example.software_management.Controller;

import com.example.software_management.DTO.AlertDTO;
import com.example.software_management.Service.AlertService;
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
     * @param userId 用户ID
     * @param deviceName 设备名称（可选，模糊匹配）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 未确认的警报分页结果
     */
    @GetMapping("/getUnconfirmedAlerts")
    public ResponseEntity<Map<String, Object>> getUnconfirmedAlerts(
            @RequestAttribute("userId") Integer userId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

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
     * @param userId 用户ID
     * @return 确认结果
     */
    @PostMapping("/confirmAlert")
    public ResponseEntity<Map<String, Object>> confirmAlerts(
            @RequestBody Map<String, List<Integer>> requestBody,
            @RequestAttribute("userId") Integer userId) {

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
     * @param userId 用户ID
     * @return XLSX文件
     */
    @GetMapping("/exportAlertsToXLSX")
    public ResponseEntity<Resource> exportAlertsToXLSX(
            @RequestAttribute("userId") Integer userId) {

        Resource resource = alertService.exportAlertsToXLSX(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=unconfirmed_alerts_" + userId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * 4.5 预警状态分布
     * @param userId 用户ID
     * @return 各状态的警报数量
     */
    @GetMapping("/status-summary")
    public ResponseEntity<Map<String, Object>> getAlertStatusSummary(
            @RequestAttribute("userId") Integer userId) {

        List<Map<String, Object>> statusSummary = alertService.getAlertStatusSummary(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statusSummary", statusSummary);

        return ResponseEntity.ok(response);
    }
}