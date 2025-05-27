//==============================================
// 预测分析模块 - Service
//==============================================

package com.example.software_management.Service;

import com.example.software_management.Model.Prediction;
import org.springframework.web.multipart.MultipartFile;

public interface PredictionService {
    /**
     * 执行预测分析
     * @param modelId 模型ID
     * @param componentId 设备ID
     * @param file 上传文件
     * @param username 用户名
     * @return 预测结果
     */
    Prediction predict(int modelId, int componentId, MultipartFile file, String username);
}