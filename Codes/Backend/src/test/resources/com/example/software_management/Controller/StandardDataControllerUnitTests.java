package com.example.software_management.Controller;

import com.example.software_management.Service.ModelService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StandardDataControllerUnitTests {

    @Mock
    private ModelService modelService;

    @InjectMocks
    private ModelController modelController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Order(1)
    @DisplayName("UT_STANDARD_DATA_CTRL_001: 基本功能测试-默认参数")
    public void testGetStandardDatasWithDefaultParameters() {
        // 准备模拟的服务响应
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("total", 32);
        List<Map<String, Object>> models = new ArrayList<>();
        Map<String, Object> model = new HashMap<>();
        model.put("id", 1);
        model.put("name", "Standard Data 1");
        model.put("type", "Type 1");
        models.add(model);
        serviceResponse.put("models", models);

        // 模拟服务方法
        when(modelService.getModels(1, 10, null)).thenReturn(serviceResponse);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response = modelController.getModels(1, 10, null);

        // 验证响应状态和内容
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals(32, ((Number) response.getBody().get("total")).intValue());

        // 验证服务方法被调用
        verify(modelService, times(1)).getModels(1, 10, null);
    }

    @Test
    @Order(2)
    @DisplayName("UT_STANDARD_DATA_CTRL_002: 分页参数测试-有效参数")
    public void testGetStandardDatasWithValidPaginationParameters() {
        // 准备模拟的服务响应
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("total", 32);
        serviceResponse.put("models", new ArrayList<>());

        // 模拟服务方法
        when(modelService.getModels(2, 15, null)).thenReturn(serviceResponse);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response = modelController.getModels(2, 15, null);

        // 验证响应状态和内容
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证服务方法被调用
        verify(modelService, times(1)).getModels(2, 15, null);
    }

    @Test
    @Order(3)
    @DisplayName("UT_STANDARD_DATA_CTRL_003: 搜索功能测试-有搜索词")
    public void testGetStandardDatasWithSearchQuery() {
        // 准备模拟的服务响应
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("total", 2);
        serviceResponse.put("models", new ArrayList<>());

        // 模拟服务方法
        when(modelService.getModels(1, 10, "涡轮")).thenReturn(serviceResponse);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response = modelController.getModels(1, 10, "涡轮");

        // 验证响应状态和内容
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证服务方法被调用
        verify(modelService, times(1)).getModels(1, 10, "涡轮");
    }

    @Test
    @Order(4)
    @DisplayName("UT_STANDARD_DATA_CTRL_004: 异常测试-Service异常")
    public void testGetStandardDatasWithServiceException() {
        // 模拟服务方法抛出异常
        when(modelService.getModels(1, 10, null)).thenThrow(new RuntimeException("Service error"));

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response = modelController.getModels(1, 10, null);

        // 验证响应状态和内容
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Service error", response.getBody().get("message"));

        // 验证服务方法被调用
        verify(modelService, times(1)).getModels(1, 10, null);
    }

    @Test
    @Order(5)
    @DisplayName("UT_STANDARD_DATA_CTRL_005: 边界测试-零或负值参数")
    public void testGetStandardDatasWithZeroOrNegativeParameters() {
        // 准备模拟的服务响应
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("total", 32);
        serviceResponse.put("models", new ArrayList<>());

        // 模拟服务方法 - 注意这里直接传递原始参数
        when(modelService.getModels(0, -5, null)).thenReturn(serviceResponse);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response = modelController.getModels(0, -5, null);

        // 验证响应状态和内容
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证服务方法被调用 - 控制器应直接传递参数到服务层
        verify(modelService, times(1)).getModels(0, -5, null);
    }
}