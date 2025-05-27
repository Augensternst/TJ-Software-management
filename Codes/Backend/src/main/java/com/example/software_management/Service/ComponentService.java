package com.example.software_management.Service;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.DTO.ReportDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ComponentService {

    /**
     * 获取当前用户拥有的设备数
     * @param userId 用户ID
     * @return 设备数量
     */
    long getUserDeviceCount(Integer userId);

    /**
     * 获取当前用户的测点数（=设备数 × 8）
     * @param userId 用户ID
     * @return 测点数量
     */
    long getUserDataPointCount(Integer userId);

    /**
     * 获取当前用户的所有设备列表
     * @param userId 用户ID
     * @param searchQuery 搜索关键词
     * @param page 页码
     * @param pageSize 每页条数
     * @return 设备列表和分页信息
     */
    Page<ComponentDTO> getUserDevices(Integer userId, String searchQuery, int page, int pageSize);

    /**
     * 获取用户所有设备的状态分布
     * @param userId 用户ID
     * @return 状态分布列表
     */
    List<Map<String, Object>> getUserDeviceStatusSummary(Integer userId);

    /**
     * 获取当前用户所有有缺陷的设备（状态 ≠ 1）
     * @param userId 用户ID
     * @return 有缺陷的设备列表
     */
    List<ComponentDTO> getUserDefectiveDevices(Integer userId);

    /**
     * 获取设备健康数据（最近7天）
     * @param deviceId 设备ID
     * @return 健康数据列表
     */
    List<Double> getDeviceHealthData(Integer deviceId);

    /**
     * 获取设备能耗数据（最近7天）
     * @param deviceId 设备ID
     * @return 能耗数据列表和总成本
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