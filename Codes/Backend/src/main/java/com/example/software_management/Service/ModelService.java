package com.example.software_management.Service;

import java.util.Map;

public interface ModelService {
    /**
     * 获取模型列表，支持分页和搜索
     * @param page 当前页码
     * @param pageSize 每页数量
     * @param searchQuery 搜索关键词
     * @return 包含模型列表和总数的映射
     */
    public Map<String, Object> getModels(int page, int pageSize, String searchQuery);
}
