package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Model.Data;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DataServiceImpl implements DataService {

    private final DataRepository dataRepository;

    @Autowired
    public DataServiceImpl(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public List<Double> getDeviceHealthData(Integer deviceId) {
        // 获取最近7天的健康指数数据
        return dataRepository.getHealthValues(deviceId);
    }

    @Override
    public ReportDTO getDeviceEnergyData(Integer deviceId) {
        // 获取最近7天的能耗数据
        List<Double> energyValues = dataRepository.getEnergyValues(deviceId);

        // 获取当日能耗成本
        Double energyCost = dataRepository.getCurrentEnergyCost(deviceId);
        if (energyCost == null) {
            energyCost = 0.0;
        }

        // 构建响应对象
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setValues(energyValues);
        reportDTO.setEnergyCost(energyCost);

        return reportDTO;
    }

    @Override
    public ReportDTO getDeviceMetricCards(Integer deviceId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 获取最新的设备数据
        Page<Data> dataPage = dataRepository.getComponentMetricData(deviceId, pageable);

        List<DataDTO> metricCards = new ArrayList<>();
        int totalPages = 0;

        if (!dataPage.isEmpty()) {
            Data latestData = dataPage.getContent().get(0);

            // 构建指标卡片数据
            // 1. 高压涡轮效率
            if (latestData.getHptEffMod() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压涡轮效率",
                        latestData.getHptEffMod(),
                        "%",
                        calculateHealthIndex(latestData.getHptEffMod(), 60, 100)
                ));
            }

            // 2. 风扇转速
            if (latestData.getNf() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇转速",
                        latestData.getNf(),
                        "RPM",
                        calculateHealthIndex(latestData.getNf(), 1000, 3000)
                ));
            }

            // 3. 风扇裕度
            if (latestData.getSmFan() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇裕度",
                        latestData.getSmFan(),
                        "%",
                        calculateHealthIndex(latestData.getSmFan(), 40, 100)
                ));
            }

            // 4. 风扇出口温度
            if (latestData.getT24() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇出口温度",
                        latestData.getT24(),
                        "°C",
                        calculateHealthIndex(latestData.getT24(), 20, 80)
                ));
            }

            // 5. 燃油流量
            if (latestData.getWf() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "燃油流量",
                        latestData.getWf(),
                        "kg/s",
                        calculateHealthIndex(latestData.getWf(), 0, 30)
                ));
            }

            // 6. HPT出口温度
            if (latestData.getT48() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "HPT出口温度",
                        latestData.getT48(),
                        "°C",
                        calculateHealthIndex(latestData.getT48(), 500, 1000)
                ));
            }

            // 7. 高压压气机转速
            if (latestData.getNc() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机转速",
                        latestData.getNc(),
                        "RPM",
                        calculateHealthIndex(latestData.getNc(), 1000, 3000)
                ));
            }

            // 8. 高压压气机裕度
            if (latestData.getSmHPC() != null) {
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机裕度",
                        latestData.getSmHPC(),
                        "%",
                        calculateHealthIndex(latestData.getSmHPC(), 40, 100)
                ));
            }

            // 计算总页数 - 假设每个参数是一个卡片
            totalPages = (int) Math.ceil(8.0 / pageSize);
        }

        // 根据分页参数裁剪结果
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, metricCards.size());

        // 确保fromIndex不超出范围
        if (fromIndex < metricCards.size()) {
            metricCards = metricCards.subList(fromIndex, toIndex);
        } else {
            metricCards = new ArrayList<>();
        }

        // 构建响应对象
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setItems(metricCards);
        reportDTO.setTotalPages(totalPages);

        return reportDTO;
    }

    @Override
    public List<DataDTO> getDeviceAttributes(Integer deviceId) {
        // 获取设备的所有属性数据
        List<Map<String, Object>> attributes = dataRepository.getComponentAttributes(deviceId);

        List<DataDTO> attributeDTOs = new ArrayList<>();
        for (Map<String, Object> attribute : attributes) {
            String name = (String) attribute.get("name");
            Double value = (Double) attribute.get("value");

            attributeDTOs.add(DataDTO.createAttributeDTO(name, value));
        }

        return attributeDTOs;
    }

    @Override
    public Double getDeviceOverallHealth(Integer deviceId) {
        // 获取最新的设备数据
        Optional<Data> latestDataOpt = dataRepository.findFirstByComponentIdOrderByTimeDesc(deviceId);

        if (latestDataOpt.isPresent()) {
            Data latestData = latestDataOpt.get();
            return latestData.calculateHealthIndex();
        }

        return null;
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

        // 如果在正常范围内，计算百分比
        if (value >= min && value <= max) {
            return (int) (((value - min) / (max - min)) * 100);
        }

        // 超出范围时返回-1
        return -1;
    }
}