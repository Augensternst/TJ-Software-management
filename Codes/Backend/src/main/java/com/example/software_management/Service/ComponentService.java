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
     * 根据设备ID获取设备基本信息
     * @param deviceId 设备ID
     * @return 设备基本信息
     */
    Map<String, Object> getDeviceById(Integer deviceId);
}