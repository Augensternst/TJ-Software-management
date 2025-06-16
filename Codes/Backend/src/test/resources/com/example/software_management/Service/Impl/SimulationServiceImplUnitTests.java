package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Model.Forecast;
import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Repository.ForecastRepository;
import com.example.software_management.Repository.ModelRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimulationServiceImplUnitTests {

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

    // 测试目录和文件
    private String testUploadDir = "./test_upload/";
    private String testModelDir = "./test_model/";
    private String testImageDir = "./test_image/";
    private String testFlaskApiUrl = "http://localhost:5000/test_predict";

    // 测试用常量
    private final int VALID_MODEL_ID = 1;
    private final int INVALID_MODEL_ID = 999;
    private final int MISSING_FILE_MODEL_ID = 2;
    private final int VALID_DEVICE_ID = 1;
    private final int INVALID_DEVICE_ID = 999;
    private final int NO_IMAGE_DEVICE_ID = 3;
    private final int MISSING_IMAGE_DEVICE_ID = 4;
    private final int ENGINE_DEVICE_ID = 5;
    private final int FAN_DEVICE_ID = 6;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 创建一个spy对象
        simulationService = spy(simulationService);

        // 设置测试目录
        simulationService.setUploadDir(testUploadDir);
        simulationService.setModelDir(testModelDir);
        simulationService.setImageDir(testImageDir);
        simulationService.setFlaskApiUrl(testFlaskApiUrl);

        // 创建测试目录
        Files.createDirectories(Paths.get(testUploadDir));
        Files.createDirectories(Paths.get(testModelDir, "CNN"));
        Files.createDirectories(Paths.get(testImageDir));

        // 创建测试模型文件
        Files.write(Paths.get(testModelDir, "CNN", "model.h5"), "test model content".getBytes());

        // 创建测试图片文件
        Files.write(Paths.get(testImageDir, "valid_image.jpg"), "test image content".getBytes());

        // 设置模拟对象的行为
        setupMocks();
    }

    private void setupMocks() {
        // 设置Model仓库的模拟行为
        Model validModel = new Model();
        validModel.setId(VALID_MODEL_ID);
        validModel.setType("CNN");
        validModel.setModelfile("model.h5");

        Model missingFileModel = new Model();
        missingFileModel.setId(MISSING_FILE_MODEL_ID);
        missingFileModel.setType("CNN");
        missingFileModel.setModelfile("missing_model.h5");

        when(modelRepository.findById(VALID_MODEL_ID)).thenReturn(Optional.of(validModel));
        when(modelRepository.findById(MISSING_FILE_MODEL_ID)).thenReturn(Optional.of(missingFileModel));
        when(modelRepository.findById(INVALID_MODEL_ID)).thenReturn(Optional.empty());

        // 设置Component仓库的模拟行为
        Component validComponent = new Component();
        validComponent.setId(VALID_DEVICE_ID);
        validComponent.setName("Valid Component");
        validComponent.setPic("valid_image.jpg");

        Component noImageComponent = new Component();
        noImageComponent.setId(NO_IMAGE_DEVICE_ID);
        noImageComponent.setName("No Image Component");
        noImageComponent.setPic(null);

        Component missingImageComponent = new Component();
        missingImageComponent.setId(MISSING_IMAGE_DEVICE_ID);
        missingImageComponent.setName("Missing Image Component");
        missingImageComponent.setPic("missing_image.jpg");

        Component engineComponent = new Component();
        engineComponent.setId(ENGINE_DEVICE_ID);
        engineComponent.setName("Engine Component");
        engineComponent.setPic("valid_image.jpg");

        Component fanComponent = new Component();
        fanComponent.setId(FAN_DEVICE_ID);
        fanComponent.setName("Fan Component");
        fanComponent.setPic("valid_image.jpg");

        when(componentRepository.findById(VALID_DEVICE_ID)).thenReturn(Optional.of(validComponent));
        when(componentRepository.findById(NO_IMAGE_DEVICE_ID)).thenReturn(Optional.of(noImageComponent));
        when(componentRepository.findById(MISSING_IMAGE_DEVICE_ID)).thenReturn(Optional.of(missingImageComponent));
        when(componentRepository.findById(ENGINE_DEVICE_ID)).thenReturn(Optional.of(engineComponent));
        when(componentRepository.findById(FAN_DEVICE_ID)).thenReturn(Optional.of(fanComponent));
        when(componentRepository.findById(INVALID_DEVICE_ID)).thenReturn(Optional.empty());

        // 设置Forecast仓库的模拟行为
        when(forecastRepository.save(any(Forecast.class))).thenAnswer(i -> {
            Forecast f = i.getArgument(0);
            f.setId(1);
            return f;
        });

        // 设置Data仓库的模拟行为
        when(dataRepository.save(any(Data.class))).thenAnswer(i -> i.getArgument(0));
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

    // 创建一个有效的CSV文件
    private MultipartFile createValidCsvFile() {
        return new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8").getBytes()
        );
    }

    // 创建一个空文件
    private MultipartFile createEmptyFile() {
        return new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );
    }

    @Test
    @Order(1)
    @DisplayName("UT_SIM_SERV_001: 标准数据验证测试-无效ID")
    public void testValidateModelWithInvalidId() {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.getSimulationResult(INVALID_MODEL_ID, VALID_DEVICE_ID, file);
        });

        assertEquals("Standard data does not exist", exception.getMessage());
        verify(modelRepository, times(1)).findById(INVALID_MODEL_ID);
    }

    @Test
    @Order(2)
    @DisplayName("UT_SIM_SERV_002: 标准数据验证测试-文件不存在")
    public void testValidateModelWithMissingFile() {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.getSimulationResult(MISSING_FILE_MODEL_ID, VALID_DEVICE_ID, file);
        });

        assertTrue(exception.getMessage().startsWith("Standard data does not exist:"));
        verify(modelRepository, times(1)).findById(MISSING_FILE_MODEL_ID);
    }

    @Test
    @Order(3)
    @DisplayName("UT_SIM_SERV_003: 设备验证测试-无效ID")
    public void testValidateComponentWithInvalidId() {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.getSimulationResult(VALID_MODEL_ID, INVALID_DEVICE_ID, file);
        });

        assertEquals("Device does not exist", exception.getMessage());
        verify(modelRepository, times(1)).findById(VALID_MODEL_ID);
        verify(componentRepository, times(1)).findById(INVALID_DEVICE_ID);
    }

    @Test
    @Order(4)
    @DisplayName("UT_SIM_SERV_004: 设备图片测试-无图片")
    public void testValidateComponentWithNoImage() throws Exception {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 模拟方法行为
        doReturn("test_file.csv").when(simulationService).saveUploadedFile(any(MultipartFile.class));
        doNothing().when(simulationService).validateCsvFile(anyString());
        doNothing().when(simulationService).saveDataFromFile(anyString(), any(Component.class));

        Map<String, Object> mockApiResponse = new HashMap<>();
        mockApiResponse.put("damage_location", "Test Location");
        mockApiResponse.put("predicted_rul", 3000);
        mockApiResponse.put("health_index", 80);
        doReturn(mockApiResponse).when(simulationService).processData(anyString(), anyString());

        Forecast mockForecast = new Forecast();
        mockForecast.setDamageLocation("Test Location");
        mockForecast.setLifeForecast(3000.0);
        mockForecast.setHealthIndex(80);
        doReturn(mockForecast).when(simulationService).saveForecastResult(
                any(Component.class), any(Model.class), any(Map.class), anyString());

        // 调用真实的buildResponse方法
        doCallRealMethod().when(simulationService).buildResponse(anyString(), any(Map.class), any(Forecast.class));

        // 执行测试
        Map<String, Object> result = simulationService.getSimulationResult(VALID_MODEL_ID, NO_IMAGE_DEVICE_ID, file);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("/images/default_component.png", result.get("imageUrl"));
        assertEquals("Test Location", result.get("damageLocation"));
        assertEquals(3000, result.get("lifespan"));
        assertEquals(80, result.get("healthIndex"));

        // 验证方法调用
        verify(modelRepository, times(1)).findById(VALID_MODEL_ID);
        verify(componentRepository, times(1)).findById(NO_IMAGE_DEVICE_ID);
    }

    @Test
    @Order(5)
    @DisplayName("UT_SIM_SERV_005: 设备图片测试-图片不存在")
    public void testValidateComponentWithMissingImage() throws Exception {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 模拟方法行为
        doReturn("test_file.csv").when(simulationService).saveUploadedFile(any(MultipartFile.class));
        doNothing().when(simulationService).validateCsvFile(anyString());
        doNothing().when(simulationService).saveDataFromFile(anyString(), any(Component.class));

        Map<String, Object> mockApiResponse = new HashMap<>();
        mockApiResponse.put("damage_location", "Test Location");
        mockApiResponse.put("predicted_rul", 3000);
        mockApiResponse.put("health_index", 80);
        doReturn(mockApiResponse).when(simulationService).processData(anyString(), anyString());

        Forecast mockForecast = new Forecast();
        mockForecast.setDamageLocation("Test Location");
        mockForecast.setLifeForecast(3000.0);
        mockForecast.setHealthIndex(80);
        doReturn(mockForecast).when(simulationService).saveForecastResult(
                any(Component.class), any(Model.class), any(Map.class), anyString());

        // 调用真实的buildResponse方法
        doCallRealMethod().when(simulationService).buildResponse(anyString(), any(Map.class), any(Forecast.class));

        // 执行测试
        Map<String, Object> result = simulationService.getSimulationResult(
                VALID_MODEL_ID, MISSING_IMAGE_DEVICE_ID, file);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("/images/default_component.png", result.get("imageUrl"));

        // 验证方法调用
        verify(modelRepository, times(1)).findById(VALID_MODEL_ID);
        verify(componentRepository, times(1)).findById(MISSING_IMAGE_DEVICE_ID);
    }

    @Test
    @Order(6)
    @DisplayName("UT_SIM_SERV_006: 文件验证测试-空文件")
    public void testSaveUploadedFileWithEmptyFile() {
        // 准备空文件
        MultipartFile emptyFile = createEmptyFile();

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.saveUploadedFile(emptyFile);
        });

        assertEquals("No file provided or file is empty", exception.getMessage());
    }

    @Test
    @Order(7)
    @DisplayName("UT_SIM_SERV_007: 文件验证测试-只有表头")
    public void testValidateCsvFileWithHeaderOnly() throws IOException {
        // 创建只有表头的CSV文件
        Path filePath = Paths.get(testUploadDir, "header_only.csv");
        Files.write(filePath, "hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc".getBytes());

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.validateCsvFile(filePath.toString());
        });

        assertEquals("File contains only header, no data rows", exception.getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("UT_SIM_SERV_008: 文件验证测试-非CSV")
    public void testValidateCsvFileWithNonCsvFile() throws IOException {
        // 创建非CSV文件
        Path filePath = Paths.get(testUploadDir, "test.txt");
        Files.write(filePath, "This is not a CSV file".getBytes());

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.validateCsvFile(filePath.toString());
        });

        assertEquals("Invalid file format. Only CSV files are supported", exception.getMessage());
    }

    @Test
    @Order(9)
    @DisplayName("UT_SIM_SERV_009: 文件验证测试-多行数据")
    public void testValidateCsvFileWithMultipleDataRows() throws IOException {
        // 创建多行数据的CSV文件
        Path filePath = Paths.get(testUploadDir, "multiple_rows.csv");
        Files.write(filePath,
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8\n" +
                        "0.9,1.0,1.1,1.2,1.3,1.4,1.5,1.6").getBytes());

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.validateCsvFile(filePath.toString());
        });

        assertEquals("Invalid file format. Only one data row is allowed", exception.getMessage());
    }

    @Test
    @Order(10)
    @DisplayName("UT_SIM_SERV_010: 文件验证测试-缺少列")
    public void testValidateCsvFileWithMissingColumns() throws IOException {
        // 创建缺少必要列的CSV文件
        Path filePath = Paths.get(testUploadDir, "missing_columns.csv");
        Files.write(filePath,
                ("header1,header2,t24,wf\n" +
                        "0.1,0.2,0.3,0.4").getBytes());

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.validateCsvFile(filePath.toString());
        });

        assertTrue(exception.getMessage().startsWith("Missing required columns:"));
    }

    @Test
    @Order(11)
    @DisplayName("UT_SIM_SERV_011: 数据验证测试-数据列不全")
    public void testSaveDataFromFileWithIncompletData() throws IOException {
        // 创建数据列不全的CSV文件
        Path filePath = Paths.get(testUploadDir, "incomplete_data.csv");
        Files.write(filePath,
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3").getBytes());

        Component component = new Component();
        component.setId(VALID_DEVICE_ID);

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.saveDataFromFile(filePath.toString(), component);
        });

        assertEquals("Error processing file data: Incomplete data row: fewer values than headers - java.lang.Exception", exception.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("UT_SIM_SERV_012: 数据验证测试-非数值数据")
    public void testSetDoubleValueIfExistsWithInvalidFormat() {
        // 准备测试数据
        Map<String, Integer> columnIndices = new HashMap<>();
        columnIndices.put("test_column", 0);
        String[] values = {"not_a_number"};

        // 创建消费者
        java.util.function.Consumer<Double> setter = value -> {};

        // 验证异常抛出
        Exception exception = assertThrows(NumberFormatException.class, () -> {
            simulationService.setDoubleValueIfExists(columnIndices, values, "test_column", setter);
        });

        assertTrue(exception.getMessage().contains("Invalid numeric format"));
    }

    @Test
    @Order(13)
    @DisplayName("UT_SIM_SERV_013: 数据验证测试-列不存在")
    public void testSetDoubleValueIfExistsWithMissingColumn() {
        // 准备测试数据
        Map<String, Integer> columnIndices = new HashMap<>();
        columnIndices.put("existing_column", 0);
        String[] values = {"123.45"};

        // 创建消费者
        java.util.function.Consumer<Double> setter = value -> {};

        // 验证异常抛出
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationService.setDoubleValueIfExists(columnIndices, values, "missing_column", setter);
        });

        assertEquals("Required column not found: missing_column", exception.getMessage());
    }

    @Test
    @Order(14)
    @DisplayName("UT_SIM_SERV_014: 不同设备类型测试-发动机")
    public void testProcessDataWithEngineComponent() {
        // 调用处理数据方法
        Map<String, Object> result = simulationService.processData("CNN", "Engine Component");

        // 验证结果
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("High Pressure Turbine", result.get("damage_location"));
        assertTrue(result.containsKey("predicted_rul"));
        assertTrue(result.containsKey("health_index"));
        assertEquals("CNN", result.get("model_used"));
        assertEquals(30, result.get("sequence_length"));
        assertEquals("Prediction successful", result.get("message"));
    }

    @Test
    @Order(15)
    @DisplayName("UT_SIM_SERV_015: 不同设备类型测试-风扇")
    public void testProcessDataWithFanComponent() {
        // 调用处理数据方法
        Map<String, Object> result = simulationService.processData("CNN", "Fan Component");

        // 验证结果
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("Fan Blade", result.get("damage_location"));
    }

    @Test
    @Order(16)
    @DisplayName("UT_SIM_SERV_016: 异常测试-文件大小超限")
    public void testSaveUploadedFileWithLargeFile() {
        // 创建一个模拟的大文件 (超过16MB)
        MockMultipartFile largeFile = mock(MockMultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(17L * 1024 * 1024); // 17MB
        when(largeFile.getOriginalFilename()).thenReturn("large_file.csv");

        // 验证异常抛出
        Exception exception = assertThrows(Exception.class, () -> {
            simulationService.saveUploadedFile(largeFile);
        });

        assertEquals("File is too large. Maximum size allowed is 16MB", exception.getMessage());
    }

    @Test
    @Order(17)
    @DisplayName("UT_SIM_SERV_017: 测试保存预测结果")
    public void testSaveForecastResult() {
        // 准备测试数据
        Component component = new Component();
        component.setId(VALID_DEVICE_ID);

        Model model = new Model();
        model.setId(VALID_MODEL_ID);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("predicted_rul", 3500);
        apiResponse.put("damage_location", "High Pressure Turbine");
        apiResponse.put("health_index", 85);

        String imageUrl = "/test/image.jpg";

        // 调用测试方法
        Forecast result = simulationService.saveForecastResult(component, model, apiResponse, imageUrl);

        // 验证结果
        assertNotNull(result);
        assertEquals(3500.0, result.getLifeForecast());
        assertEquals("High Pressure Turbine", result.getDamageLocation());
        assertEquals(85, result.getHealthIndex());
        assertEquals(component, result.getComponent());
        assertEquals(model, result.getModel());

        verify(forecastRepository, times(1)).save(any(Forecast.class));
    }

    @Test
    @Order(18)
    @DisplayName("UT_SIM_SERV_018: 测试构建响应")
    public void testBuildResponse() {
        // 准备测试数据
        String imageUrl = "/test/image.jpg";

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("predicted_rul", 3500);
        apiResponse.put("damage_location", "High Pressure Turbine");
        apiResponse.put("health_index", 85);

        Forecast forecast = new Forecast();
        forecast.setDamageLocation("High Pressure Turbine");
        forecast.setLifeForecast(3500.0);
        forecast.setHealthIndex(85);

        // 调用测试方法
        Map<String, Object> result = simulationService.buildResponse(imageUrl, apiResponse, forecast);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(imageUrl, result.get("imageUrl"));
        assertEquals("High Pressure Turbine", result.get("damageLocation"));
        assertEquals(3500, result.get("lifespan"));
        assertEquals(85, result.get("healthIndex"));
    }

    @Test
    @Order(19)
    @DisplayName("UT_SIM_SERV_019: 测试保存数据文件方法")
    public void testSaveDataFromFile() throws Exception {
        // 准备测试数据
        Component component = new Component();
        component.setId(VALID_DEVICE_ID);

        // 创建有效CSV文件
        Path filePath = Paths.get(testUploadDir, "valid_data.csv");
        Files.write(filePath,
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8").getBytes());

        // 模拟数据保存
        doAnswer(invocation -> {
            Data data = invocation.getArgument(0);
            assertEquals(0.1, data.getHptEffMod());
            assertEquals(0.2, data.getNf());
            assertEquals(0.3, data.getSmFan());
            assertEquals(0.4, data.getT24());
            assertEquals(0.5, data.getWf());
            assertEquals(0.6, data.getT48());
            assertEquals(0.7, data.getNc());
            assertEquals(0.8, data.getSmHPC());
            return data;
        }).when(dataRepository).save(any(Data.class));

        // 调用测试方法
        simulationService.saveDataFromFile(filePath.toString(), component);

        // 验证保存方法被调用
        verify(dataRepository, times(1)).save(any(Data.class));
    }

    @Test
    @Order(20)
    @DisplayName("UT_SIM_SERV_020: 完整流程测试-成功场景")
    public void testGetSimulationResultCompleteSuccess() throws Exception {
        // 准备测试文件
        MultipartFile file = createValidCsvFile();

        // 设置saveUploadedFile返回一个有效文件名
        String validFilePath = Paths.get(testUploadDir, "valid.csv").toString();
        Files.write(Paths.get(validFilePath),
                ("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8").getBytes());
        doReturn("valid.csv").when(simulationService).saveUploadedFile(file);

        // 确保validateCsvFile不做任何事情
        doNothing().when(simulationService).validateCsvFile(anyString());

        // 确保saveDataFromFile不做任何事情
        doNothing().when(simulationService).saveDataFromFile(anyString(), any(Component.class));

        // 设置processData使用真实方法
        doCallRealMethod().when(simulationService).processData(anyString(), anyString());

        // 设置saveForecastResult使用真实方法
        doCallRealMethod().when(simulationService).saveForecastResult(
                any(Component.class), any(Model.class), any(Map.class), anyString());

        // 设置buildResponse使用真实方法
        doCallRealMethod().when(simulationService).buildResponse(anyString(), any(Map.class), any(Forecast.class));

        // 执行测试
        Map<String, Object> result = simulationService.getSimulationResult(
                VALID_MODEL_ID, ENGINE_DEVICE_ID, file);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("High Pressure Turbine", result.get("damageLocation"));
        assertNotNull(result.get("lifespan"));
        assertNotNull(result.get("healthIndex"));

        // 验证方法调用
        verify(modelRepository, times(1)).findById(VALID_MODEL_ID);
        verify(componentRepository, times(1)).findById(ENGINE_DEVICE_ID);
        verify(forecastRepository, times(1)).save(any(Forecast.class));
    }
}