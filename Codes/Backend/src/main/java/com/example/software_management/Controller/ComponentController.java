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
@RequestMapping("/api/devices")
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
    @GetMapping("/count")
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
     * @param requestBody 包含搜索和分页参数的请求体
     * @return 设备列表和分页信息
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> getUserDevices(
            @RequestBody Map<String, Object> requestBody) {

        int userId = GetInfo.getCurrentUserId();

        // 从请求体中提取参数
        String searchQuery = (String) requestBody.getOrDefault("searchQuery", null);

        // 获取分页参数，如果不存在使用默认值
        int page = requestBody.containsKey("page") ?
                Integer.parseInt(requestBody.get("page").toString()) : 1;
        int pageSize = requestBody.containsKey("pageSize") ?
                Integer.parseInt(requestBody.get("pageSize").toString()) : 10;

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
    @GetMapping("/status-summary")
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
    @GetMapping("/defective")
    public ResponseEntity<Map<String, Object>> getUserDefectiveDevices() {
        int userId = GetInfo.getCurrentUserId();
        List<ComponentDTO> defectiveDevices = componentService.getUserDefectiveDevices(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("devices", defectiveDevices);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据设备ID获取设备基本信息
     * @param deviceId 设备ID
     * @return 设备基本信息
     */
    @GetMapping("/get-device")
    public ResponseEntity<Map<String, Object>> getDeviceById(@RequestParam Integer deviceId) {
        Map<String, Object> result = componentService.getDeviceById(deviceId);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


}