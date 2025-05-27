package com.example.software_management.Controller;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.User;
import com.example.software_management.Security.UserSecurity;
import com.example.software_management.Service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
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

@RestController
@RequestMapping("/component")
public class ComponentController {

    private final ComponentService componentService;

    @Autowired
    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @PostMapping(value = "/upload/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadComponent(
            @RequestParam("pic") MultipartFile pic,
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam(value = "model", required = false) Integer modelId,
            @RequestParam(value = "description", required = false) String description) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            ComponentDTO component = componentService.uploadComponent(pic, name, location, modelId, description, currentUser);

            response.put("success", true);
            response.put("data", component);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("code", e.getMessage().contains("组件名已存在") ? 110 : 103);
            response.put("message", e.getMessage());
        } catch (IOException e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "文件处理错误: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "上传组件失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/all_user/")
    public Map<String, Object> getUserComponents() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            List<Map<String, Object>> components = componentService.getUserComponents(currentUser);

            response.put("success", true);
            response.put("data", components);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取组件列表失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/model_count/")
    public Map<String, Object> getModelCount() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            Map<String, Integer> modelCount = componentService.getModelCount(currentUser);

            response.put("success", true);
            response.put("data", modelCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取模型组件数量统计失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/location_count/")
    public Map<String, Object> getLocationCount() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            Map<String, Integer> locationCount = componentService.getLocationCount(currentUser);

            response.put("success", true);
            response.put("data", locationCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取位置组件数量统计失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/pic/{id}/")
    public ResponseEntity<?> getComponentPic(@PathVariable Integer id) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            byte[] pic = componentService.getComponentPic(id, currentUser);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(pic);
        } catch (SecurityException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 124);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 104);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取组件图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete/{id}/")
    public Map<String, Object> deleteComponent(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            boolean deleted = componentService.deleteComponent(id, currentUser);

            if (deleted) {
                response.put("success", true);
                response.put("data", "删除成功");
            } else {
                response.put("success", false);
                response.put("code", 104);
                response.put("message", "组件不存在");
            }
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("code", 124);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "删除组件失败: " + e.getMessage());
        }

        return response;
    }
}