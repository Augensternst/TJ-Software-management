package com.example.software_management.Controller;

import com.example.software_management.Model.Model;
import com.example.software_management.Model.User;
import com.example.software_management.Security.UserSecurity;
import com.example.software_management.Service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/model")
public class ModelController {

    private final ModelService modelService;

    @Autowired
    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping("/all/")
    public Map<String, Object> getAllModels() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> models = modelService.getAllModels();
            response.put("success", true);
            response.put("data", models);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型列表失败: " + e.getMessage());
        }
        return response;
    }


    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> createModel(
            @RequestPart("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("style") String style,
            @RequestParam("status") String status,
            @RequestParam("description") String description) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(authentication.getPrincipal());
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User user = userSecurity.getUser();  // 直接获取 User 对象

            Model model = modelService.createModel(file, name, style, status, description, user);

            // 不返回模型文件内容
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("id", model.getId());
            modelData.put("name", model.getName());
            modelData.put("style", model.getStyle());
            modelData.put("status", model.getStatus());
            modelData.put("description", model.getDescription());
            modelData.put("uploadedTime", model.getUploadedTime());
            modelData.put("md5", model.getMd5());

            response.put("success", true);
            response.put("data", modelData);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("code", e.getMessage().contains("模型名已存在") ? 100 : 101);
            response.put("message", e.getMessage());
        } catch (IOException e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "文件处理错误: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "创建模型失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/style/")
    public Map<String, Object> getStylePercentage() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Double> stylePercentage = modelService.getStylePercentage();
            response.put("success", true);
            response.put("data", stylePercentage);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型风格统计失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/status/")
    public Map<String, Object> getStatusPercentage() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Double> statusPercentage = modelService.getStatusPercentage();
            response.put("success", true);
            response.put("data", statusPercentage);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型状态统计失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/{id}/")
    public Map<String, Object> getModelById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Model> modelOptional = modelService.getModelById(id);

            if (modelOptional.isPresent()) {
                Model model = modelOptional.get();

                // 不返回模型文件内容
                Map<String, Object> modelData = new HashMap<>();
                modelData.put("id", model.getId());
                modelData.put("name", model.getName());
                modelData.put("style", model.getStyle());
                modelData.put("status", model.getStatus());
                modelData.put("description", model.getDescription());
                modelData.put("uploadedTime", model.getUploadedTime());
                modelData.put("md5", model.getMd5());
                if (model.getUser() != null) {
                    modelData.put("user", model.getUser().getUsername());
                }

                response.put("success", true);
                response.put("data", modelData);
            } else {
                response.put("success", false);
                response.put("code", 103);
                response.put("message", "模型不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型详情失败: " + e.getMessage());
        }

        return response;
    }

    @DeleteMapping("/{id}/")
    public Map<String, Object> deleteModel(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            boolean deleted = modelService.deleteModel(id, username);

            if (deleted) {
                response.put("success", true);
                response.put("data", "删除成功!");
            } else {
                response.put("success", false);
                response.put("code", 500);
                response.put("message", "删除失败");
            }
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("code", 123);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "删除模型失败: " + e.getMessage());
        }

        return response;
    }

    // 获取模型文件的端点 - 可以用于下载模型
    @GetMapping("/{id}/file/")
    public ResponseEntity<?> getModelFile(@PathVariable Integer id) {
        try {
            Optional<Model> modelOptional = modelService.getModelById(id);

            if (modelOptional.isPresent()) {
                Model model = modelOptional.get();

                // 返回文件内容
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + model.getName() + ".model\"")
                        .body(model.getModelfile());
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 103);
                response.put("message", "模型不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}