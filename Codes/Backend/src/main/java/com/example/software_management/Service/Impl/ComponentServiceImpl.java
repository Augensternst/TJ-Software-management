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
    private final DataRepository dataRepository;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, DataRepository dataRepository) {
        this.componentRepository = componentRepository;
        this.dataRepository = dataRepository;
    }

    @Override
    public long getUserDeviceCount(Integer userId) {
        return componentRepository.countByUserId(userId);
    }

    @Override
    public long getUserDataPointCount(Integer userId) {
        // 测点数 = 设备数 × 8
        return getUserDeviceCount(userId) * 8;
    }

    @Override
    public Page<ComponentDTO> getUserDevices(Integer userId, String searchQuery, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Component> componentPage = componentRepository.findByUserIdWithSearch(
                userId, searchQuery, pageable);

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
    public List<Double> getDeviceHealthData(Integer deviceId) {
        return componentRepository.getComponentHealthTrend(deviceId);
    }

    @Override
    public ReportDTO getDeviceEnergyData(Integer deviceId) {
        List<Double> energyValues = componentRepository.getComponentEnergyTrend(deviceId);
        Double energyCost = componentRepository.getComponentEnergyCost(deviceId);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setValues(energyValues);
        reportDTO.setEnergyCost(energyCost != null ? energyCost : 0.0);

        return reportDTO;
    }

    @Override
    public ReportDTO getDeviceMetricCards(Integer deviceId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 获取指标卡片数据
        Page<Data> dataPage = dataRepository.getComponentMetricData(deviceId, pageable);

        // 将数据转换为DTO
        List<DataDTO> metricCards = new ArrayList<>();
        if (!dataPage.isEmpty()) {
            Data latestData = dataPage.getContent().get(0);

            // 添加温度指标
            if (latestData.getT24() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "温度", latestData.getT24(), "°C", calculateHealthIndex(latestData.getT24(), 20, 60)));
            }

            // 添加湿度指标（这里假设某个字段代表湿度，实际应根据实际情况调整）
            if (latestData.getSmFan() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇裕度", latestData.getSmFan(), "%", calculateHealthIndex(latestData.getSmFan(), 40, 100)));
            }

            // 添加转速指标
            if (latestData.getNf() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇转速", latestData.getNf(), "RPM", calculateHealthIndex(latestData.getNf(), 1000, 3000)));
            }

            // 添加燃油流量指标
            if (latestData.getWf() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "燃油流量", latestData.getWf(), "kg/s", calculateHealthIndex(latestData.getWf(), 0, 20)));
            }

            // 添加高压涡轮效率指标
            if (latestData.getHptEffMod() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压涡轮效率", latestData.getHptEffMod(), "%", calculateHealthIndex(latestData.getHptEffMod(), 60, 100)));
            }

            // 添加高压压气机裕度指标
            if (latestData.getSmHPC() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机裕度", latestData.getSmHPC(), "%", calculateHealthIndex(latestData.getSmHPC(), 40, 100)));
            }

            // 添加高压压气机转速指标
            if (latestData.getNc() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机转速", latestData.getNc(), "RPM", calculateHealthIndex(latestData.getNc(), 1000, 3000)));
            }

            // 添加HPT出口温度指标
            if (latestData.getT48() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "HPT出口温度", latestData.getT48(), "°C", calculateHealthIndex(latestData.getT48(), 500, 1000)));
            }
        }

        // 计算总页数
        int totalPages = componentRepository.getComponentMetricCardsTotalPages(deviceId, pageSize);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setItems(metricCards);
        reportDTO.setTotalPages(totalPages);

        return reportDTO;
    }

    /**
     * 根据指标值计算健康指数
     * @param value 指标值
     * @param min 最小正常值
     * @param max 最大正常值
     * @return 健康指数，-1表示无需展示
     */
    private Integer calculateHealthIndex(Double value, double min, double max) {
        if (value == null) {
            return -1;
        }

        // 简单实现：如果在正常范围内，计算百分比；否则返回-1
        if (value >= min && value <= max) {
            return (int) (((value - min) / (max - min)) * 100);
        } else {
            return -1;
        }
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