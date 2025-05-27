package com.example.software_management.Controller;

import com.example.software_management.Exception.ResourceNotFoundException;
import com.example.software_management.Model.User;
import com.example.software_management.Security.UserSecurity;
import com.example.software_management.Service.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private static final Logger logger = LoggerFactory.getLogger(PredictionController.class);
    private final PredictionService predictionService;

    @Autowired
    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * 执行预测分析
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> predict(
            @RequestParam(value = "modelId", required = true) Integer modelId,
            @RequestParam(value = "componentId", required = true) Integer componentId,
            @RequestParam(value = "file", required = true) MultipartFile file
            ) {

        logger.info("接收到预测分析请求: modelId={}, componentId={}, fileName={}",
                modelId, componentId, file.getOriginalFilename());

        try {
            // 验证文件
            if (file.isEmpty()) {
                logger.warn("未上传文件或文件为空");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MISSING_FILE", "请上传预测数据文件"));
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".csv")) {
                logger.warn("不支持的文件类型: {}", originalFilename);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("UNSUPPORTED_FILE_TYPE", "不支持的文件类型，请上传CSV文件"));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();
            String username= currentUser.getUsername();

            // 执行预测
            Prediction prediction = predictionService.predict(
                    modelId, componentId, file, username);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, Object> data = new HashMap<>();
            data.put("predictionId", prediction.getId());
            data.put("result", prediction.getResult());
            data.put("confidence", prediction.getConfidence());
            data.put("timestamp", prediction.getPredictionTime().toString());

            response.put("data", data);

            logger.info("预测分析成功: predictionId={}", prediction.getId());
            return ResponseEntity.ok(response);

        } catch (MultipartException e) {
            logger.error("文件上传错误", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("FILE_UPLOAD_ERROR", "文件上传错误: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("参数错误", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("INVALID_PARAMETER", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            logger.warn("资源未找到", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            logger.error("执行预测分析时发生错误", e);
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