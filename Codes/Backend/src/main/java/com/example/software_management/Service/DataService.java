package com.example.software_management.Service;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;

import java.util.List;

public interface DataService {

    /**
     * 获取设备健康数据（最近7天）
     * @param deviceId 设备ID
     * @return 健康数据列表
     */
    List<Double> getDeviceHealthData(Integer deviceId);

    /**
     * 获取设备能耗数据（最近7天）
     * @param deviceId 设备ID
     * @return 能耗数据列表和当日成本
     */
    ReportDTO getDeviceEnergyData(Integer deviceId);

    /**
     * 获取设备指标卡片数据
     * @param deviceId 设备ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 指标卡片数据
     */
    ReportDTO getDeviceMetricCards(Integer deviceId, int page, int pageSize);


}