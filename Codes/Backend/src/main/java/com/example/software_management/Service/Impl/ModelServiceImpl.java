package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.DataService;
import com.example.software_management.Service.ModelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelServiceImpl implements ModelService {
    private final ModelRepository modelRepository;
    public ModelServiceImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Map<String, Object> getModels(int page, int pageSize, String searchQuery) {
        Map<String, Object> response = new HashMap<>();

        // 验证页码和每页大小参数
        if (page < 1) {
            page = 1; // 如果页码小于1，则使用默认值1
        }

        if (pageSize < 1) {
            pageSize = 10; // 如果每页大小小于1，则使用默认值10
        } else if (pageSize > 50) {
            pageSize = 50; // 如果每页大小大于50，则使用最大值50
        }

        // 创建分页请求
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 根据是否有搜索词选择不同查询
        Page<Model> modelsPage;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            modelsPage = modelRepository.findAll(pageable);

        } else {
            modelsPage = modelRepository.findByNameContaining(searchQuery, pageable);

        }
        System.out.println("====================================================");
        System.out.println(modelsPage.getTotalElements());
        System.out.println(modelsPage);
        System.out.println("====================================================");

        // 准备返回数据
        List<Map<String, Object>> modelsList = new ArrayList<>();
        for (Model model : modelsPage.getContent()) {
            Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("id", model.getId());
            modelMap.put("name", model.getName());
            modelMap.put("type", model.getType());
            modelsList.add(modelMap);
        }

        response.put("success", true);
        response.put("total", modelsPage.getTotalElements());
        response.put("models", modelsList);

        return response;
    }
}
