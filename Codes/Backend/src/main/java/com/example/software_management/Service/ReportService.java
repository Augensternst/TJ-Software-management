package com.example.software_management.Service;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

public interface ReportService {

    /**
     * 获取用户今日警报统计
     * @param userId 用户ID
     * @return 今日警报统计
     */
    Map<String, Long> getTodayAlertStats(Integer userId);

    /**
     * 获取用户所有警报统计（全局）
     * @param userId 用户ID
     * @return 所有警报统计
     */
    Map<String, Long> getAllAlertStats(Integer userId);

    /**
     * 获取本周警报统计（按天分组）
     * @param userId 用户ID
     * @return 本周警报统计
     */
    ReportDTO getWeeklyAlertStats(Integer userId);

    /**
     * 获取设备指定属性值(8个)
     * @param deviceId 设备ID
     * @return 设备属性值列表
     */
    List<DataDTO> getDeviceAttributes(Integer deviceId);

    /**
     * 将设备属性值导出为excel文件
     * @param deviceId 设备ID
     * @return Excel文件资源
     */
    Resource exportDeviceAttributes(Integer deviceId);
}