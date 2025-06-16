package com.example.software_management.Controller;

import com.example.software_management.Service.SimulationService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimulationControllerUnitTests {

    @Mock
    private SimulationService simulationService;

    @InjectMocks
    private SimulationController simulationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Order(1)
    @DisplayName("UT_SIM_CTRL_001: 基本功能测试-正常响应")
    public void testGetSimulationResultWithNormalResponse() throws Exception {
        // 准备模拟的服务响应
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("imageUrl", "/images/device1.png");
        serviceResponse.put("damageLocation", "High Pressure Turbine");
        serviceResponse.put("lifespan", 3500);
        serviceResponse.put("healthIndex", 85);

        // 准备测试文件
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "header1,header2\nvalue1,value2".getBytes()
        );

        // 模拟服务方法
        when(simulationService.getSimulationResult(eq(1), eq(1), any(MultipartFile.class)))
                .thenReturn(serviceResponse);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, file);

        // 验证响应状态和内容
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("/images/device1.png", response.getBody().get("imageUrl"));
        assertEquals("High Pressure Turbine", response.getBody().get("damageLocation"));
        assertEquals(3500, response.getBody().get("lifespan"));
        assertEquals(85, response.getBody().get("healthIndex"));

        // 验证服务方法被调用
        verify(simulationService, times(1)).getSimulationResult(eq(1), eq(1), any(MultipartFile.class));
    }

    @Test
    @Order(2)
    @DisplayName("UT_SIM_CTRL_002: 异常测试-服务异常")
    public void testGetSimulationResultWithServiceException() throws Exception {
        // 准备测试文件
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "header1,header2\nvalue1,value2".getBytes()
        );

        // 模拟服务方法抛出异常
        when(simulationService.getSimulationResult(eq(1), eq(1), any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Service error"));

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, file);

        // 验证响应状态和内容
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Service error", response.getBody().get("message"));

        // 验证服务方法被调用
        verify(simulationService, times(1)).getSimulationResult(eq(1), eq(1), any(MultipartFile.class));
    }

    @Test
    @Order(3)
    @DisplayName("UT_SIM_CTRL_003: 边界测试-文件为空")
    public void testGetSimulationResultWithEmptyFile() throws Exception {
        // 准备空文件
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        // 模拟服务方法抛出异常
        when(simulationService.getSimulationResult(eq(1), eq(1), eq(emptyFile)))
                .thenThrow(new RuntimeException("No file provided or file is empty"));

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, emptyFile);

        // 验证响应状态和内容
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("No file provided or file is empty", response.getBody().get("message"));

        // 验证服务方法被调用
        verify(simulationService, times(1)).getSimulationResult(eq(1), eq(1), eq(emptyFile));
    }
}