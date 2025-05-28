package com.example.software_management.Service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import com.example.software_management.Model.Model;

import java.util.Map;

public interface SimulationService {
    /**
     * 获取模型列表，支持分页和搜索
     * @param page 当前页码
     * @param pageSize 每页数量
     * @param searchQuery 搜索关键词
     * @return 包含模型列表和总数的映射
     */
    Map<String, Object> getModels(int page, int pageSize, String searchQuery);

    /**
     * 获取模拟结果
     * @param modelId 模型ID
     * @param deviceId 设备ID
     * @param file 上传的数据文件
     * @return 包含模拟结果的映射
     */
    Map<String, Object> getSimulationResult(int modelId, int deviceId, MultipartFile file) throws Exception;
}