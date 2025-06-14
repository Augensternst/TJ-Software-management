package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Model.Forecast;
import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Repository.ForecastRepository;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.SimulationService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SimulationServiceImpl implements SimulationService {

    private final ModelRepository modelRepository;
    private final ComponentRepository componentRepository;
    private final ForecastRepository forecastRepository;
    private final DataRepository dataRepository; // 新增DataRepository


    // 添加setter方法用于测试
    // 配置信息
    @Setter
    @Value("${app.upload.dir:./upload_file/}")
    private String uploadDir;

    @Setter
    @Value("${app.model.dir:F:\\AAAFourthGrade\\SoftwareManagement\\TJ-Software-management\\Codes\\AI\\training_model}")
    private String modelDir;

    @Setter
    @Value("${app.image.dir:./image}")
    private String imageDir;

    @Setter
    @Value("${app.flask.api.url:http://localhost:5000/predict}")
    private String flaskApiUrl;

    @Autowired
    public SimulationServiceImpl(
            ModelRepository modelRepository,
            ComponentRepository componentRepository,
            ForecastRepository forecastRepository,
            DataRepository dataRepository, // 新增DataRepository
            RestTemplate restTemplate) {
        this.modelRepository = modelRepository;
        this.componentRepository = componentRepository;
        this.forecastRepository = forecastRepository;
        this.dataRepository = dataRepository; // 注入DataRepository
    }

    @PostConstruct
    public void init() {
        // 在所有依赖注入完成后创建目录
        createDirectoryIfNotExists(uploadDir);
        createDirectoryIfNotExists(modelDir);
        createDirectoryIfNotExists(imageDir);
    }

    private void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + dirPath);
            }
        }
    }



    @Override
    public Map<String, Object> getSimulationResult(int modelId, int deviceId, MultipartFile file) throws Exception {
        // 1. 验证模型和设备
        ModelValidationResult modelValidation = validateModel(modelId);
        ComponentValidationResult componentValidation = validateComponent(deviceId);

        // 2. 保存上传的文件
        String fileName = saveUploadedFile(file);
        String filePath = Paths.get(uploadDir, fileName).toString();

        // 3. 验证CSV文件格式和内容 - 确保只有一行数据
        validateCsvFile(filePath);

        // 4. 读取表格数据并保存到Data表
        saveDataFromFile(filePath, componentValidation.getComponent());

        // 5. 调用模拟的Flask API - 返回默认值而非真实调用
        Map<String, Object> apiResponse = processData(
                modelValidation.getModelType(),
                componentValidation.getComponent().getName()
        );

        // 6. 记录预测结果到forecast表
        Forecast forecast = saveForecastResult(
                componentValidation.getComponent(),
                modelValidation.getModel(),
                apiResponse,
                componentValidation.getImageUrl()
        );

        // 7. 构建响应
        return buildResponse(
                componentValidation.getImageUrl(),
                apiResponse,
                forecast
        );
    }

    /**
     * 验证CSV文件格式和内容
     */
    void validateCsvFile(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取表头行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new Exception("File is empty");
            }

            // 检查文件扩展名
            if (!filePath.toLowerCase().endsWith(".csv")) {
                throw new Exception("Invalid file format. Only CSV files are supported");
            }

            // 读取数据行数
            String dataLine = reader.readLine();
            if (dataLine == null) {
                throw new Exception("File contains only header, no data rows");
            }

            // 确保只有一行数据
            String nextLine = reader.readLine();
            if (nextLine != null) {
                throw new Exception("Invalid file format. Only one data row is allowed");
            }

            // 检查必要的列是否存在
            String[] headers = headerLine.toLowerCase().split(",");
            Set<String> headerSet = new HashSet<>(Arrays.asList(headers));

            // 定义必要的列
            Set<String> requiredColumns = new HashSet<>(Arrays.asList(
                    "hpt_eff_mod", "nf", "smfan", "t24", "wf", "t48", "nc", "smhpc"
            ));

            // 检查是否缺少必要的列
            requiredColumns.removeAll(headerSet);
            if (!requiredColumns.isEmpty()) {
                throw new Exception("Missing required columns: " + requiredColumns);
            }

        } catch (IOException e) {
            throw new Exception("Failed to read file: " + e.getMessage());
        }
    }

    /**
     * 验证模型是否存在且有效
     */
    private ModelValidationResult validateModel(int modelId) throws Exception {
        // 查询完整的模型对象
        Optional<Model> modelOptional = modelRepository.findById(modelId);
        if (modelOptional.isEmpty()) {
            throw new Exception("Standard data does not exist");
        }
        Model model = modelOptional.get();

        // 检查模型文件是否存在
        Path modelPath = Paths.get(modelDir, model.getType(), model.getModelfile());
        if (!Files.exists(modelPath)) {
            throw new Exception("Standard data does not exist: " + modelPath);
        }

        return new ModelValidationResult(model, modelPath.toString(), model.getType());
    }

    /**
     * 验证设备是否存在且有效
     */
    private ComponentValidationResult validateComponent(int deviceId) throws Exception {
        // 查询设备信息
        Optional<Component> componentOptional = componentRepository.findById(deviceId);
        if (componentOptional.isEmpty()) {
            throw new Exception("Device does not exist");
        }
        Component component = componentOptional.get();

        // 获取设备图片URL
        String imageUrl = component.getPic();
        if (imageUrl == null || imageUrl.isEmpty()) {
            // 如果设备没有图片，使用默认图片
            imageUrl = "/images/default_component.png";
        } else {
            // 检查图片文件是否存在
            Path imagePath = Paths.get(imageDir, imageUrl);
            if (!Files.exists(imagePath)) {
                // 如果图片文件不存在，使用默认图片
                imageUrl = "/images/default_component.png";
            }
        }

        return new ComponentValidationResult(component, imageUrl);
    }

    /**
     * 保存上传的文件
     */
    String saveUploadedFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("No file provided or file is empty");
        }

        // 检查文件大小 (16MB限制)
        if (file.getSize() > 16 * 1024 * 1024) {
            throw new Exception("File is too large. Maximum size allowed is 16MB");
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new Exception("Failed to save uploaded file: " + e.getMessage());
        }
    }

    void saveDataFromFile(String filePath, Component component) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取表头行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new Exception("File is empty");
            }

            // 使用逗号分隔符分割表头
            String[] headers = headerLine.split(",");

            // 创建列索引映射，同时转换为小写以便不区分大小写比较
            Map<String, Integer> columnIndices = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                columnIndices.put(header, i);
                System.out.println("Column name: " + header + ", index: " + i); // 调试信息
            }

            // 读取唯一的数据行
            String line = reader.readLine();
            if (line == null) {
                throw new Exception("File contains only header, no data rows");
            }

            // 使用逗号分隔符分割数据
            String[] values = line.split(",");

            if (values.length < headers.length) {
                throw new Exception("Incomplete data row: fewer values than headers");
            }

            // 创建新的Data实体
            Data data = new Data();
            data.setComponent(component);
            data.setTime(LocalDateTime.now());
            data.setFile(filePath);

            // 设置各属性值 - 使用小写键名查找
            setDoubleValueIfExists(columnIndices, values, "hpt_eff_mod", data::setHptEffMod);
            setDoubleValueIfExists(columnIndices, values, "nf", data::setNf);
            setDoubleValueIfExists(columnIndices, values, "smfan", data::setSmFan);
            setDoubleValueIfExists(columnIndices, values, "t24", data::setT24);
            setDoubleValueIfExists(columnIndices, values, "wf", data::setWf);
            setDoubleValueIfExists(columnIndices, values, "t48", data::setT48);
            setDoubleValueIfExists(columnIndices, values, "nc", data::setNc);
            setDoubleValueIfExists(columnIndices, values, "smhpc", data::setSmHPC);

            // 打印设置的值，用于调试
            System.out.println("Data set: " +
                    "HPT_eff_mod=" + data.getHptEffMod() + ", " +
                    "Nf=" + data.getNf() + ", " +
                    "SmFan=" + data.getSmFan() + ", " +
                    "T24=" + data.getT24() + ", " +
                    "Wf=" + data.getWf() + ", " +
                    "T48=" + data.getT48() + ", " +
                    "Nc=" + data.getNc() + ", " +
                    "SmHPC=" + data.getSmHPC());

            // 保存数据
            dataRepository.save(data);

        } catch (IOException e) {
            throw new Exception("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error processing file data: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }

    void setDoubleValueIfExists(Map<String, Integer> columnIndices, String[] values,
                                String columnName, java.util.function.Consumer<Double> setter) {
        // 使用小写列名查找
        if (columnIndices.containsKey(columnName) && columnIndices.get(columnName) < values.length) {
            String value = values[columnIndices.get(columnName)].trim();
            if (!value.isEmpty()) {
                try {
                    double doubleValue = Double.parseDouble(value);
                    setter.accept(doubleValue);
                    System.out.println("Setting " + columnName + " = " + doubleValue); // 调试信息
                } catch (NumberFormatException e) {
                    System.out.println("Cannot parse value: " + columnName + " = " + value);
                    throw new NumberFormatException("Invalid numeric format for column " + columnName + ": " + value);
                }
            }
        } else {
            System.out.println("Column not found: " + columnName + " or index out of range");
            throw new IllegalArgumentException("Required column not found: " + columnName);
        }
    }

    /**
     * 模拟Flask API响应 - 返回与Python后端相同格式的模拟数据
     */
    Map<String, Object> processData(String modelType, String componentName) {
        // 创建模拟响应
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");

        // 根据组件名称生成不同的损伤位置
        String damageLocation;
        if (componentName.toLowerCase().contains("engine") || componentName.toLowerCase().contains("motor")) {
            damageLocation = "High Pressure Turbine";
        } else if (componentName.toLowerCase().contains("fan")) {
            damageLocation = "Fan Blade";
        } else if (componentName.toLowerCase().contains("compressor")) {
            damageLocation = "Compressor Blade";
        } else if (componentName.toLowerCase().contains("pump")) {
            damageLocation = "Impeller";
        } else if (componentName.toLowerCase().contains("turbine")) {
            damageLocation = "Turbine Blade";
        } else {
            damageLocation = "Main Bearing";
        }
        mockResponse.put("damage_location", damageLocation);

        // 生成随机的预测剩余使用寿命 (RUL) - 范围在1000到5000之间
        float predictedRul = 1000.0f + new Random().nextInt(4000);
        mockResponse.put("predicted_rul", predictedRul);

        // 生成随机的健康指数 - 范围在60到95之间
        int healthIndex = 60 + new Random().nextInt(36);
        mockResponse.put("health_index", healthIndex);

        // 添加与Python后端完全一致的其他字段
        mockResponse.put("model_used", modelType);
        mockResponse.put("sequence_length", 30);  // 默认序列长度
        mockResponse.put("message", "Prediction successful");  // 英文版本的预测成功消息

        return mockResponse;
    }

    /**
     * 保存预测结果到数据库 - 适配Forecast表结构
     */
    Forecast saveForecastResult(Component component, com.example.software_management.Model.Model modelObj, Map<String, Object> apiResponse, String imageUrl) {
        // 创建新的预测记录
        Forecast forecast = new Forecast();
        forecast.setComponent(component);
        forecast.setModel(modelObj);
        forecast.setForecastTime(LocalDateTime.now());

        // 设置从Python API获取的预测值
        if (apiResponse.containsKey("predicted_rul")) {
            forecast.setLifeForecast(((Number) apiResponse.get("predicted_rul")).doubleValue());  // 使用lifeForecast字段
        } else {
            forecast.setLifeForecast(0.0);  // 默认值
        }

        if (apiResponse.containsKey("damage_location")) {
            forecast.setDamageLocation((String) apiResponse.get("damage_location"));
        } else {
            forecast.setDamageLocation("No specific damage detected");
        }

        if (apiResponse.containsKey("health_index")) {
            forecast.setHealthIndex(((Number) apiResponse.get("health_index")).intValue());
        } else {
            forecast.setHealthIndex(0);
        }

        // 保存预测结果 - @PostPersist会自动创建告警
        return forecastRepository.save(forecast);
    }

    /**
     * 构建响应数据
     */
    Map<String, Object> buildResponse(String imageUrl, Map<String, Object> apiResponse, Forecast forecast) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("imageUrl", imageUrl);

        // 使用Python API返回的损伤位置
        response.put("damageLocation", forecast.getDamageLocation());

        // 使用Python API返回的预测剩余寿命
        response.put("lifespan", forecast.getLifeForecast().intValue());

        // 使用Python API返回的健康指数
        response.put("healthIndex", forecast.getHealthIndex());

        return response;
    }

    // 内部辅助类，用于封装验证结果
    @Getter
    private static class ModelValidationResult {
        private final Model model;
        private final String modelPath;
        private final String modelType;

        public ModelValidationResult(Model model, String modelPath, String modelType) {
            this.model = model;
            this.modelPath = modelPath;
            this.modelType = modelType;
        }
    }

    @Getter
    private static class ComponentValidationResult {
        private final Component component;
        private final String imageUrl;

        public ComponentValidationResult(Component component, String imageUrl) {
            this.component = component;
            this.imageUrl = imageUrl;
        }
    }
}