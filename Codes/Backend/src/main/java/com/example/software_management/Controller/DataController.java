package com.example.software_management.Controller;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.Model.DData;
import com.example.software_management.Model.User;
import com.example.software_management.Security.UserSecurity;
import com.example.software_management.Service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import java.util.Optional;

@RestController
@RequestMapping("/data")
public class DataController {

    private final DataService dataService;

    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> createData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("component") Integer componentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            DData data = dataService.createData(file, name, componentId, currentUser);

            response.put("success", true);
            response.put("data", data.getId());
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("code", 104);
            response.put("message", e.getMessage());
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("code", 124);
            response.put("message", e.getMessage());
        } catch (IOException e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "文件处理错误: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "上传数据失败: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/user_component/{id}/")
    public Object getUserComponentData(@PathVariable Integer id) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            return dataService.getUserComponentData(id, currentUser);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 104);
            response.put("message", e.getMessage());
            return response;
        } catch (SecurityException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 124);
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取组件数据失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/download/{id}/")
    public ResponseEntity<?> downloadData(@PathVariable Integer id) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            Optional<DData> dataOpt = dataService.downloadData(id, currentUser);

            if (dataOpt.isPresent()) {
                DData data = dataOpt.get();

                ByteArrayResource resource = new ByteArrayResource(data.getFile());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + data.getName())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(data.getFile().length)
                        .body(resource);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", 105);
                response.put("message", "数据不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (SecurityException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 125);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "下载数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/all_user/")
    public Map<String, Object> getAllUserData() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            List<Map<String, Object>> datas = dataService.getAllUserData(currentUser);

            response.put("success", true);
            response.put("data", datas);
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "获取用户数据失败: " + e.getMessage());
        }

        return response;
    }

    @DeleteMapping("/{id}/")
    public Map<String, Object> deleteData(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            User currentUser=userSecurity.getUser();

            boolean deleted = dataService.deleteData(id, currentUser);

            if (deleted) {
                response.put("success", true);
                response.put("data", "删除成功");
            } else {
                response.put("success", false);
                response.put("code", 500);
                response.put("message", "删除失败");
            }
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("code", 125);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("code", 500);
            response.put("message", "删除数据失败: " + e.getMessage());
        }

        return response;
    }
}