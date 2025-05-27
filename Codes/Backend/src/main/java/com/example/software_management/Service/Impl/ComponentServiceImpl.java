package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Model;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;
    private final ModelRepository modelRepository;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, ModelRepository modelRepository) {
        this.componentRepository = componentRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    @Transactional
    public ComponentDTO uploadComponent(MultipartFile pic, String name, String location,
                                     Integer modelId, String description, User currentUser) throws IOException {
        // 检查组件名是否重复
        if (componentRepository.existsByNameAndUser(name, currentUser)) {
            throw new IllegalArgumentException("组件名已存在");
        }

        Component component = new Component();
        component.setName(name);
        component.setLocation(location);
        component.setPic(pic.getBytes());
        component.setUser(currentUser);

        // 如果提供了模型ID，关联模型
        if (modelId != null) {
            Optional<Model> modelOpt = modelRepository.findById(modelId);
            if (modelOpt.isEmpty()) {
                throw new IllegalArgumentException("模型不存在");
            }

            component.setModel(modelOpt.get());
            component.setDescription(description);
        }

        // 保存组件
        Component savedComponent = componentRepository.save(component);

        return new ComponentDTO(component);
    }

    @Override
    public List<Map<String, Object>> getUserComponents(User currentUser) {
        return componentRepository.findByUserWithoutSensitiveData(currentUser);
    }

    @Override
    public Map<String, Integer> getModelCount(User currentUser) {
        List<Map<String, Object>> results = componentRepository.countComponentsByModel(currentUser);

        Map<String, Integer> modelCount = new HashMap<>();
        for (Map<String, Object> result : results) {
            String modelName = (String) result.get("name");
            Long count = (Long) result.get("count");
            modelCount.put(modelName, count.intValue());
        }

        return modelCount;
    }

    @Override
    public Map<String, Integer> getLocationCount(User currentUser) {
        List<Map<String, Object>> results = componentRepository.countComponentsByLocation(currentUser);

        Map<String, Integer> locationCount = new HashMap<>();
        for (Map<String, Object> result : results) {
            String location = (String) result.get("location");
            Long count = (Long) result.get("count");
            locationCount.put(location, count.intValue());
        }

        return locationCount;
    }

    @Override
    public byte[] getComponentPic(Integer id, User currentUser) {
        Optional<Component> componentOpt = componentRepository.findByIdWithUser(id);

        if (componentOpt.isEmpty()) {
            throw new IllegalArgumentException("组件不存在");
        }

        Component component = componentOpt.get();

        // 检查是否是当前用户的组件
        if (!component.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权访问其它用户的组件信息");
        }

        return component.getPic();
    }

    @Override
    @Transactional
    public boolean deleteComponent(Integer id, User currentUser) {
        Optional<Component> componentOpt = componentRepository.findByIdWithUser(id);

        if (componentOpt.isEmpty()) {
            return false;
        }

        Component component = componentOpt.get();

        // 检查是否是当前用户的组件
        if (!component.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权删除其它用户的组件信息");
        }

        componentRepository.delete(component);
        return true;
    }
}