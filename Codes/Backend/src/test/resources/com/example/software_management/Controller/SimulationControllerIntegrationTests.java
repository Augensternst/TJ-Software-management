package com.example.software_management.Controller;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.Forecast;
import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Repository.ForecastRepository;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.Impl.SimulationServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimulationControllerIntegrationTests {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SimulationServiceImpl simulationService;

    private SimulationController simulationController;

    // 测试目录和文件
    private String testUploadDir = "./test_upload/";
    private String testModelDir = "./test_model/";
    private String testImageDir = "./test_image/";
    private String testFlaskApiUrl = "http://localhost:5000/test_predict";

    // 测试数据
    private Model validModel;
    private Component validComponent;
    private Component noImageComponent;
    private Component engineComponent;
    private Component fanComponent;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 设置测试目录
        simulationService.setUploadDir(testUploadDir);
        simulationService.setModelDir(testModelDir);
        simulationService.setImageDir(testImageDir);
        simulationService.setFlaskApiUrl(testFlaskApiUrl);

        // 创建控制器并注入服务
        simulationController = new SimulationController(simulationService);

        // 创建测试目录
        Files.createDirectories(Paths.get(testUploadDir));
        Files.createDirectories(Paths.get(testModelDir, "CNN"));
        Files.createDirectories(Paths.get(testImageDir));

        // 创建测试模型文件
        Files.write(Paths.get(testModelDir, "CNN", "model.h5"), "test model content".getBytes());

        // 创建测试图片文件
        Files.write(Paths.get(testImageDir, "valid_image.jpg"), "test image content".getBytes());

        // 准备测试数据
        setupTestData();

        // 设置模拟返回值
        setupMocks();
    }

    private void setupTestData() {
        // 有效模型
        validModel = new Model();
        validModel.setId(1);
        validModel.setType("CNN");
        validModel.setModelfile("model.h5");

        // 有效设备
        validComponent = new Component();
        validComponent.setId(1);
        validComponent.setName("Test Component");
        validComponent.setPic("valid_image.jpg");

        // 无图片设备
        noImageComponent = new Component();
        noImageComponent.setId(3);
        noImageComponent.setName("No Image Component");
        noImageComponent.setPic(null);

        // 发动机设备
        engineComponent = new Component();
        engineComponent.setId(5);
        engineComponent.setName("Test Engine");
        engineComponent.setPic("valid_image.jpg");

        // 风扇设备
        fanComponent = new Component();
        fanComponent.setId(6);
        fanComponent.setName("Test Fan");
        fanComponent.setPic("valid_image.jpg");
    }

    private void setupMocks() {
        // 模拟模型仓库
        when(modelRepository.findById(1)).thenReturn(Optional.of(validModel));
        when(modelRepository.findById(999)).thenReturn(Optional.empty());

        // 模拟设备仓库
        when(componentRepository.findById(1)).thenReturn(Optional.of(validComponent));
        when(componentRepository.findById(3)).thenReturn(Optional.of(noImageComponent));
        when(componentRepository.findById(5)).thenReturn(Optional.of(engineComponent));
        when(componentRepository.findById(6)).thenReturn(Optional.of(fanComponent));
        when(componentRepository.findById(999)).thenReturn(Optional.empty());

        // 模拟预测结果保存
        when(forecastRepository.save(any(Forecast.class))).thenAnswer(invocation -> {
            Forecast forecast = invocation.getArgument(0);
            forecast.setId(1);  // 设置ID
            return forecast;
        });
    }

    @AfterEach
    public void cleanup() throws IOException {
        // 清理测试目录
        deleteDirectory(new File(testUploadDir));
        deleteDirectory(new File(testModelDir));
        deleteDirectory(new File(testImageDir));
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    // 创建有效CSV文件
    private MultipartFile createValidCsvFile() {
        return new MockMultipartFile(
                "file",
                "valid.csv",
                "text/csv",
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8").getBytes()
        );
    }

    @Test
    @Order(1)
    @DisplayName("IT_SIM_CTRL_001: 基本功能测试-正常输入")
    public void testGetSimulationResultWithValidInput() throws Exception {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertNotNull(body.get("imageUrl"));
        assertNotNull(body.get("damageLocation"));
        assertNotNull(body.get("lifespan"));
        assertNotNull(body.get("healthIndex"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(1);
        verify(componentRepository, times(1)).findById(1);
        verify(forecastRepository, times(1)).save(any(Forecast.class));
        verify(dataRepository, times(1)).save(any());
    }

    @Test
    @Order(2)
    @DisplayName("IT_SIM_CTRL_002: 标准数据验证测试-无效ID")
    public void testGetSimulationResultWithInvalidModelId() {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法 - 使用无效模型ID
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(999, 1, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Standard data does not exist", body.get("message"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(999);
        verify(componentRepository, never()).findById(anyInt());
    }

    @Test
    @Order(3)
    @DisplayName("IT_SIM_CTRL_003: 设备验证测试-无效ID")
    public void testGetSimulationResultWithInvalidDeviceId() {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法 - 使用无效设备ID
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 999, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Device does not exist", body.get("message"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(1);
        verify(componentRepository, times(1)).findById(999);
    }

    @Test
    @Order(4)
    @DisplayName("IT_SIM_CTRL_004: 设备图片测试-无图片")
    public void testGetSimulationResultWithNoImageDevice() throws Exception {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法 - 使用无图片设备
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 3, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("/images/default_component.png", body.get("imageUrl"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(1);
        verify(componentRepository, times(1)).findById(3);
    }

    @Test
    @Order(5)
    @DisplayName("IT_SIM_CTRL_005: 文件验证测试-空文件")
    public void testGetSimulationResultWithEmptyFile() {
        // 准备空文件
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, emptyFile);

        // 验证响应状态
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("No file provided or file is empty", body.get("message"));
    }

    @Test
    @Order(6)
    @DisplayName("IT_SIM_CTRL_006: 文件验证测试-非CSV")
    public void testGetSimulationResultWithNonCsvFile() {
        // 准备非CSV文件
        MultipartFile nonCsvFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "This is not a CSV file".getBytes()
        );

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 1, nonCsvFile);

        // 验证响应状态
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertTrue(body.get("message").toString().contains("Invalid file format"));
    }

    @Test
    @Order(7)
    @DisplayName("IT_SIM_CTRL_007: 不同设备类型测试-发动机")
    public void testGetSimulationResultWithEngineDevice() throws Exception {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法 - 使用发动机设备
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 5, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("High Pressure Turbine", body.get("damageLocation"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(1);
        verify(componentRepository, times(1)).findById(5);
    }

    @Test
    @Order(8)
    @DisplayName("IT_SIM_CTRL_008: 不同设备类型测试-风扇")
    public void testGetSimulationResultWithFanDevice() throws Exception {
        // 准备有效CSV文件
        MultipartFile validFile = createValidCsvFile();

        // 调用控制器方法 - 使用风扇设备
        ResponseEntity<Map<String, Object>> response =
                simulationController.getSimulationResult(1, 6, validFile);

        // 验证响应状态
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证响应内容
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Fan Blade", body.get("damageLocation"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findById(1);
        verify(componentRepository, times(1)).findById(6);
    }
}