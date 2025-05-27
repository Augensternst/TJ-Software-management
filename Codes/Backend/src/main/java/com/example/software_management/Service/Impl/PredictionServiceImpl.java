//==============================================
// 预测分析模块 - Service Implementation
//==============================================

package com.example.software_management.Service.Impl;

import com.example.software_management.Exception.ResourceNotFoundException;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Model;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Repository.UserRepository;
import com.example.software_management.Service.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PredictionServiceImpl implements PredictionService {
    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final PredictionRepository predictionRepository;
    private final ModelRepository modelRepository;
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PredictionServiceImpl(PredictionRepository predictionRepository,
                                 ModelRepository modelRepository,
                                 ComponentRepository componentRepository,
                                 UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.modelRepository = modelRepository;
        this.componentRepository = componentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Prediction predict(int modelId, int componentId, MultipartFile file, String username) {
        logger.debug("执行预测分析: modelId={}, componentId={}, username={}, fileName={}",
                modelId, componentId, username, file.getOriginalFilename());

        // 参数验证
        if (file.isEmpty()) {
            logger.error("上传的文件为空");
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB限制
            logger.error("文件过大: {}MB", file.getSize() / (1024 * 1024));
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        try {
            // 获取模型、设备和用户信息
            Model model = modelRepository.findById(modelId)
                    .orElseThrow(() -> {
                        logger.error("模型未找到: ID={}", modelId);
                        return new ResourceNotFoundException("模型未找到: ID=" + modelId);
                    });

            Component component = componentRepository.findById(componentId)
                    .orElseThrow(() -> {
                        logger.error("设备未找到: ID={}", componentId);
                        return new ResourceNotFoundException("设备未找到: ID=" + componentId);
                    });

            User user = null;
            if (username != null && !username.isEmpty()) {
                user = userRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            logger.error("用户未找到: username={}", username);
                            return new ResourceNotFoundException("用户未找到: " + username);
                        });
            }

            logger.info("开始执行预测分析: 模型={}, 设备={}, 用户={}",
                    model.getName(), component.getName(), username);

            // 创建预测记录
            Prediction prediction = new Prediction();
            prediction.setModel(model);
            prediction.setComponent(component);
            prediction.setCreatedBy(user);
            prediction.setPredictionTime(LocalDateTime.now());

            // 存储文件
            prediction.setInputFile(file.getBytes());

            // 执行预测模型处理
            performPrediction(prediction, file);

            // 保存预测结果
            Prediction savedPrediction = predictionRepository.save(prediction);

            logger.info("预测分析完成: ID={}, 结果={}, 置信度={}",
                    savedPrediction.getId(), savedPrediction.getResult(), savedPrediction.getConfidence());

            return savedPrediction;

        } catch (IOException e) {
            logger.error("处理上传文件时发生错误", e);
            throw new RuntimeException("处理上传文件时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 模拟预测过程
     * 注：实际系统中，这里应该调用真实的预测模型处理上传的数据
     */
    private void performPrediction(Prediction prediction, MultipartFile file) {
        logger.debug("执行预测模型处理: 文件名={}, 大小={}KB",
                file.getOriginalFilename(), file.getSize() / 1024);

        try {
            // 模拟处理延迟
            Thread.sleep(1000);

            // 模拟预测结果
            Random random = new Random();
            double confidence = 0.5 + random.nextDouble() * 0.5; // 50% ~ 100%

            double predictedLifetime = 5000;
            String resultText;
            if (confidence > 0.9) {
                resultText = "预测结果：设备寿命预计为 " + predictedLifetime + " 小时，可靠性高。";
            } else if (confidence > 0.7) {
                resultText = "预测结果：设备寿命预计为 " + predictedLifetime + " 小时，可靠性中等。置信度: " + String.format("%.2f", confidence * 100) + "%";
            } else {
                resultText = "预测结果：设备寿命预计为 " + predictedLifetime + " 小时，可靠性较低。建议尽早维护或更换。置信度: " + String.format("%.2f", confidence * 100) + "%";
            }



            // 设置预测结果
            prediction.setResult(resultText);
            prediction.setConfidence(confidence);

            logger.debug("预测结果生成: 结果={}, 置信度={}", resultText, confidence);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("预测过程被中断", e);
            throw new RuntimeException("预测过程被中断", e);
        }
    }
}
