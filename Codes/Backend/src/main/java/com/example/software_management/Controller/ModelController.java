package com.example.software_management.Controller;

import com.example.software_management.Service.ModelService;
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
@RequestMapping("/api/standard")
public class ModelController {

    private final ModelService modelService;

    @Autowired
    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * 获取模型列表
     * @param page 当前页码
     * @param pageSize 每页数量
     * @param searchQuery 搜索关键词
     * @return 模型列表和总数
     */
    @GetMapping("/getDatas")
    public ResponseEntity<Map<String, Object>> getModels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String searchQuery) {

        try {
            Map<String, Object> response = modelService.getModels(page, pageSize, searchQuery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}