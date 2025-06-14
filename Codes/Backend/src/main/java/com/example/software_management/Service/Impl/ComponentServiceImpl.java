package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;


    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, DataRepository dataRepository) {
        this.componentRepository = componentRepository;

    }

    @Override
    public long getUserDeviceCount(Integer userId) {
        return componentRepository.countByUserId(userId);
    }


    @Override
    public Page<ComponentDTO> getUserDevices(Integer userId, String searchQuery, int page, int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        System.out.println(pageable);

        Page<Component> componentPage;
        if (searchQuery == null || searchQuery.isEmpty()) {
            componentPage = componentRepository.findByUserId(userId, pageable);
        } else {
            componentPage = componentRepository.findByUserIdAndNameContaining(userId, searchQuery, pageable);
        }

        List<ComponentDTO> componentDTOs = componentPage.getContent().stream()
                .map(ComponentDTO::createSimplifiedDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(componentDTOs, pageable, componentPage.getTotalElements());
    }

    @Override
    public List<Map<String, Object>> getUserDeviceStatusSummary(Integer userId) {
        List<Object[]> statusSummary = componentRepository.getComponentStatusSummary(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : statusSummary) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<ComponentDTO> getUserDefectiveDevices(Integer userId) {
        List<Component> defectiveComponents = componentRepository.findDefectiveComponents(userId);

        return defectiveComponents.stream()
                .map(ComponentDTO::createDefectiveDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDeviceById(Integer deviceId) {
        // 查询设备
        Optional<Component> componentOptional = componentRepository.findById(deviceId);

        // 准备返回数据
        Map<String, Object> result = new HashMap<>();

        if (componentOptional.isPresent()) {
            Component component = componentOptional.get();
            result.put("success", true);
            result.put("deviceName", component.getName());
            result.put("picture", component.getPic());
        } else {
            result.put("success", false);
            result.put("message", "设备不存在");
        }

        return result;
    }


}