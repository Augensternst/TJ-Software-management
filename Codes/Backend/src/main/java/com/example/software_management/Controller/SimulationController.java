package com.example.software_management.Controller;

import com.example.software_management.Service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    @Autowired
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }


    /**
     * 获取模拟结果
     * @param deviceId 设备ID
     * @param file 上传的数据文件
     * @return 模拟结果
     */
    @PostMapping(value = "/getSimulationResult", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> getSimulationResult(
            @RequestParam int standardDataId,
            @RequestParam int deviceId,
            @RequestParam MultipartFile file) {

        try {
            Map<String, Object> response = simulationService.getSimulationResult(standardDataId, deviceId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}