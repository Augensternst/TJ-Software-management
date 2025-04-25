package com.example.software_management.Controller;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Security.GetInfo;
import com.example.software_management.Service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {
    private final DataService dataService;

    @Autowired
    public MonitorController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 2.1 获取设备健康数据
     * @param deviceId 设备ID
     * @return 健康数据列表
     */
    @GetMapping("/{deviceId}/health")
    public ResponseEntity<Map<String, Object>> getDeviceHealthData(@PathVariable Integer deviceId) {
        // 可以在这里添加权限检查，确保当前用户有权访问此设备
        int userId = GetInfo.getCurrentUserId();

        List<Double> healthData = dataService.getDeviceHealthData(deviceId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("values", healthData);

        return ResponseEntity.ok(response);
    }

    /**
     * 2.2 获取设备能耗数据
     * @param deviceId 设备ID
     * @return 能耗数据列表和当日成本
     */
    @GetMapping("/{deviceId}/energy")
    public ResponseEntity<Map<String, Object>> getDeviceEnergyData(@PathVariable Integer deviceId) {
        // 可以在这里添加权限检查，确保当前用户有权访问此设备
        int userId = GetInfo.getCurrentUserId();

        ReportDTO energyData = dataService.getDeviceEnergyData(deviceId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("values", energyData.getValues());
        response.put("energyCost", energyData.getEnergyCost());

        return ResponseEntity.ok(response);
    }

    /**
     * 2.3 获取设备指标卡片数据
     * @param deviceId 设备ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 指标卡片数据
     */
    @GetMapping("/{deviceId}/cards")
    public ResponseEntity<Map<String, Object>> getDeviceMetricCards(
            @PathVariable Integer deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {

        // 可以在这里添加权限检查，确保当前用户有权访问此设备
        int userId = GetInfo.getCurrentUserId();

        ReportDTO metricCards = dataService.getDeviceMetricCards(deviceId, page, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("items", metricCards.getItems());
        response.put("totalPages", metricCards.getTotalPages());

        return ResponseEntity.ok(response);
    }


}