package com.example.software_management.Service;

import com.example.software_management.DTO.AlertDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface AlertService {

    /**
     * 获取用户未确认的警报设备
     * @param userId 用户ID
     * @param deviceName 设备名称（可选，模糊匹配）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 未确认的警报分页结果
     */
    Page<AlertDTO> getUnconfirmedAlerts(
            Integer userId,
            String deviceName,
            String startTime,
            String endTime,
            int page,
            int pageSize);

    /**
     * 批量更新设备警报状态为已确认
     * @param alertIds 需要确认的报警ID列表
     * @param userId 确认人ID
     * @return 确认结果，包含成功数量和失败的ID列表
     */
    Map<String, Object> confirmAlerts(List<Integer> alertIds, Integer userId);

    /**
     * 批量删除警报
     * @param alertIds 需要删除的报警ID列表
     * @return 删除结果，包含成功数量和失败的ID列表
     */
    Map<String, Object> deleteAlerts(List<Integer> alertIds);

    /**
     * 导出用户未确认的警报设备至XLSX
     * @param userId 用户ID
     * @return 包含XLSX文件的Resource
     */
    Resource exportAlertsToXLSX(Integer userId);

    /**
     * 获取用户的警报状态分布
     * @param userId 用户ID
     * @return 各状态的警报数量
     */
    List<Map<String, Object>> getAlertStatusSummary(Integer userId);
}