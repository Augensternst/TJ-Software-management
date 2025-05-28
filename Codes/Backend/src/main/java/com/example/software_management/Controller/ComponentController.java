package com.example.software_management.Controller;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Security.GetInfo;
import com.example.software_management.Service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentService componentService;

    @Autowired
    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    /**
     * 获取当前用户拥有的设备数
     * @return 设备数量
     */
    @GetMapping("/user/devices/count")
    public ResponseEntity<Map<String, Object>> getUserDeviceCount() {
        int userId = GetInfo.getCurrentUserId();
        long deviceCount = componentService.getUserDeviceCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceCount", deviceCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户的所有设备列表
     * @param searchQuery 搜索关键词
     * @param page 页码
     * @param pageSize 每页条数
     * @return 设备列表和分页信息
     */
    @GetMapping("/user/devices")
    public ResponseEntity<Map<String, Object>> getUserDevices(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        int userId = GetInfo.getCurrentUserId();
        Page<ComponentDTO> devicePage = componentService.getUserDevices(userId, searchQuery, page, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("devices", devicePage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", devicePage.getTotalElements());
        pagination.put("page", page);
        pagination.put("pageSize", pageSize);
        pagination.put("totalPages", devicePage.getTotalPages());

        response.put("pagination", pagination);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户所有设备的状态分布
     * @return 状态分布列表
     */
    @GetMapping("/user/devices/status-summary")
    public ResponseEntity<Map<String, Object>> getUserDeviceStatusSummary() {
        int userId = GetInfo.getCurrentUserId();
        List<Map<String, Object>> statusSummary = componentService.getUserDeviceStatusSummary(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statusSummary", statusSummary);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户所有有缺陷的设备（状态 ≠ 1）
     * @return 有缺陷的设备列表
     */
    @GetMapping("/user/devices/defective")
    public ResponseEntity<Map<String, Object>> getUserDefectiveDevices() {
        int userId = GetInfo.getCurrentUserId();
        List<ComponentDTO> defectiveDevices = componentService.getUserDefectiveDevices(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("devices", defectiveDevices);

        return ResponseEntity.ok(response);
    }


    /**
     * 获取预警状态分布（复用设备状态分布接口）
     * @return 状态分布列表
     */
    @GetMapping("/alerts/status-summary")
    public ResponseEntity<Map<String, Object>> getAlertStatusSummary() {
        // 直接复用设备状态分布接口
        return getUserDeviceStatusSummary();
    }

    /**
     * 根据设备ID获取设备基本信息
     * @param deviceId 设备ID
     * @return 设备基本信息
     */
    @GetMapping("/getdevice")
    public ResponseEntity<Map<String, Object>> getDeviceById(@RequestParam Integer deviceId) {
        Map<String, Object> result = componentService.getDeviceById(deviceId);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


}