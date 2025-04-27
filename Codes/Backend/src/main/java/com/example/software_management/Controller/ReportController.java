package com.example.software_management.Controller;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Security.GetInfo;
import com.example.software_management.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 5.1 获取用户今日警报统计
     * @return 今日警报统计
     */
    @GetMapping("/alerts/getTodayAlertStats")
    public ResponseEntity<Map<String, Object>> getTodayAlertStats() {
        int userId = GetInfo.getCurrentUserId();
        Map<String, Long> stats = reportService.getTodayAlertStats(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("unconfirmedToday", stats.get("unconfirmedToday"));
        response.put("confirmedToday", stats.get("confirmedToday"));

        return ResponseEntity.ok(response);
    }

    /**
     * 5.2 获取用户所有警报统计（全局）
     * @return 所有警报统计
     */
    @GetMapping("/alerts/getAllAlertStats")
    public ResponseEntity<Map<String, Object>> getAllAlertStats() {
        int userId = GetInfo.getCurrentUserId();
        Map<String, Long> stats = reportService.getAllAlertStats(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalAlerts", stats.get("totalAlerts"));
        response.put("unconfirmed", stats.get("unconfirmed"));
        response.put("confirmed", stats.get("confirmed"));

        return ResponseEntity.ok(response);
    }

    // /**
    //  * 5.3 获取本周警报统计（按天分组）
    //  * @return 本周警报统计
    //  */
    // @GetMapping("/alerts/getWeeklyAlertStats")
    // public ResponseEntity<Map<String, Object>> getWeeklyAlertStats() {
    //     int userId = GetInfo.getCurrentUserId();
    //     ReportDTO weeklyStats = reportService.getWeeklyAlertStats(userId);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("success", true);
    //     response.put("totalWeekly", weeklyStats.getTotalWeekly());
    //     response.put("confirmedWeekly", weeklyStats.getConfirmedWeekly());
    //     response.put("unconfirmedWeekly", weeklyStats.getUnconfirmedWeekly());
    //     response.put("dailyStats", weeklyStats.getDailyStats());

    //     return ResponseEntity.ok(response);
    // }

    //  * 5.5 获取设备指定属性值(8个)
    //  * @param deviceId 设备ID
    //  * @return 设备属性值列表
    //  */
    // @GetMapping("/devices/getDeviceAttributes")
    // public ResponseEntity<Map<String, Object>> getDeviceAttributes(
    //         @RequestParam Integer deviceId) {
    //     // 可以在这里添加权限检查，确保当前用户有权访问此设备
    //     // int userId = GetInfo.getCurrentUserId();

    //     List<DataDTO> attributes = reportService.getDeviceAttributes(deviceId);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("success", true);
    //     response.put("attributes", attributes);

    //     return ResponseEntity.ok(response);
    // }

    // /**
    //  * 5.6 将设备属性值导出为excel文件
    //  * @param deviceId 设备ID
    //  * @return Excel文件
    //  */
    // @GetMapping("/devices/exportDeviceAttributes")
    // public ResponseEntity<Resource> exportDeviceAttributes(
    //         @RequestParam Integer deviceId) {
    //     // 可以在这里添加权限检查，确保当前用户有权访问此设备
    //     // int userId = GetInfo.getCurrentUserId();

    //     Resource resource = reportService.exportDeviceAttributes(deviceId);

    //     return ResponseEntity.ok()
    //             .header(HttpHeaders.CONTENT_DISPOSITION,
    //                     "attachment; filename=device_attributes_" + deviceId + ".xlsx")
    //             .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    //             .body(resource);
    // }
}