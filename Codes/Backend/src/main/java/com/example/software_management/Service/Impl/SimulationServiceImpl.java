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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final RestTemplate restTemplate;

    // 配置信息
    @Value("${app.upload.dir:./upload_file/}")
    private String uploadDir;

    @Value("${app.model.dir:F:\\AAAFourthGrade\\SoftwareManagement\\TJ-Software-management\\Codes\\AI\\training_model}")
    private String modelDir;

    @Value("${app.image.dir:./image}")
    private String imageDir;

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
        this.restTemplate = restTemplate;
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
                throw new RuntimeException("无法创建目录: " + dirPath);
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

        // 3. 读取表格数据并保存到Data表（新增功能）
        saveDataFromFile(filePath, componentValidation.getComponent());

        // 4. 调用Flask API进行预测 - 传递文件路径
        Map<String, Object> apiResponse = callFlaskApi(
                modelValidation.getModelType(),
                modelValidation.getModelPath(),
                filePath
        );

        // 5. 记录预测结果到forecast表
        Forecast forecast = saveForecastResult(
                componentValidation.getComponent(),
                modelValidation.getModel(),
                apiResponse,
                componentValidation.getImageUrl()
        );

        // 6. 构建响应
        return buildResponse(
                componentValidation.getImageUrl(),
                apiResponse,
                forecast
        );
    }

    /**
     * 验证模型是否存在且有效
     */
    private ModelValidationResult validateModel(int modelId) throws Exception {
        // 查询完整的模型对象
        Optional<Model> modelOptional = modelRepository.findById(modelId);
        if (modelOptional.isEmpty()) {
            throw new Exception("模型不存在");
        }
        Model model = modelOptional.get();

        // 检查模型文件是否存在
        Path modelPath = Paths.get(modelDir, model.getType(), model.getModelfile());
        if (!Files.exists(modelPath)) {
            throw new Exception("模型文件不存在: " + modelPath);
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
            throw new Exception("设备不存在");
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
    private String saveUploadedFile(MultipartFile file) throws Exception {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new Exception("保存上传文件失败: " + e.getMessage());
        }
    }

    private void saveDataFromFile(String filePath, Component component) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取表头行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new Exception("表格文件为空");
            }

            // 使用逗号分隔符分割表头
            String[] headers = headerLine.split(",");

            // 创建列索引映射，同时转换为小写以便不区分大小写比较
            Map<String, Integer> columnIndices = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                columnIndices.put(header, i);
                System.out.println("列名: " + header + ", 索引: " + i); // 调试信息
            }

            // 读取数据行
            String line;
            while ((line = reader.readLine()) != null) {
                // 使用逗号分隔符分割数据
                String[] values = line.split(",");

                if (values.length < headers.length) {
                    System.out.println("跳过不完整行: " + line);
                    continue;
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
                System.out.println("设置的数据: " +
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
            }
        } catch (IOException e) {
            throw new Exception("读取表格文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("处理表格数据错误: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }

    private void setDoubleValueIfExists(Map<String, Integer> columnIndices, String[] values,
                                        String columnName, java.util.function.Consumer<Double> setter) {
        // 使用小写列名查找
        if (columnIndices.containsKey(columnName) && columnIndices.get(columnName) < values.length) {
            String value = values[columnIndices.get(columnName)].trim();
            if (!value.isEmpty()) {
                try {
                    double doubleValue = Double.parseDouble(value);
                    setter.accept(doubleValue);
                    System.out.println("设置 " + columnName + " = " + doubleValue); // 调试信息
                } catch (NumberFormatException e) {
                    System.out.println("无法解析值: " + columnName + " = " + value);
                }
            }
        } else {
            System.out.println("找不到列: " + columnName + " 或索引超出范围");
        }
    }



    /**
     * 调用Flask API进行预测 - 使用文件路径而不是上传文件
     */
    private Map<String, Object> callFlaskApi(String modelType, String modelPath, String filePath) throws Exception {
        try {
            // 创建请求体 - 使用JSON格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model_type", modelType); // CNN_LSTM 或 CNN_Transformer
            requestBody.put("model_path", modelPath);
            requestBody.put("file_path", filePath);  // 直接传递文件路径
            requestBody.put("sequence_length", 30);  // 使用默认序列长度

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建HTTP实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求到Flask API
            ResponseEntity<Map> response = restTemplate.postForEntity(flaskApiUrl, requestEntity, Map.class);

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                if ("success".equals(result.get("status"))) {
                    return result;
                } else {
                    throw new Exception("预测失败: " + result.getOrDefault("message", "未知错误"));
                }
            } else {
                throw new Exception("调用预测API失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new Exception("调用Flask API出错: " + e.getMessage(), e);
        }
    }

    /**
     * 保存预测结果到数据库 - 适配Forecast表结构
     */
    private Forecast saveForecastResult(Component component, com.example.software_management.Model.Model modelObj, Map<String, Object> apiResponse, String imageUrl) {
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
            forecast.setDamageLocation("未检测到明确损伤");
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
    private Map<String, Object> buildResponse(String imageUrl, Map<String, Object> apiResponse, Forecast forecast) {
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